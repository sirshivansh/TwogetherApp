const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-twogethe-243f9980-56fe-4ebc-ae49-35dc0310da3c";
const ALICE_UID = "alice_ngo_1";
const BOB_UID = "bob_ngo_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      databaseId: DATABASE_ID,
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any Twogether collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("student_profiles").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("doubts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("uploaded_lessons").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("doubts").doc("d1").set({
      userId: ALICE_UID,
      studentProfileId: "p1",
      studentName: "Asha",
      gradeClass: 5,
      subject: "Math",
      questionText: "What is 3/4 + 1/2?",
      status: "synced",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated owner: valid student_profile create, update, and rejection of shadow fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("student_profiles").doc("prof_1").set({
      userId: ALICE_UID,
      name: "Asha",
      email: "asha.student@twogether.org",
      gradeClass: 5,
      avatarId: "lion",
      themeColorKey: "terracotta",
      preferredLanguage: "Bilingual",
      parentalConsentGiven: true,
      centerName: "Asha Kiran Kendra",
      pinSequence: "0,1,2,3",
      xpPoints: 120,
      modulesCompleted: 2,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("student_profiles").doc("prof_1").update({
      xpPoints: 180,
      modulesCompleted: 3,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field / @DocumentId 'id' leak
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("student_profiles").doc("prof_2").set({
      id: "prof_2",
      userId: ALICE_UID,
      name: "Ravi",
      email: "ravi@twogether.org",
      gradeClass: 6,
      avatarId: "panda",
      themeColorKey: "sage",
      preferredLanguage: "English",
      parentalConsentGiven: true,
      centerName: "Asha Kiran Kendra",
      pinSequence: "1,1,2,2",
      xpPoints: 0,
      modulesCompleted: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject invalid gradeClass > 10
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("student_profiles").doc("prof_3").set({
      userId: ALICE_UID,
      name: "Ravi",
      email: "ravi@twogether.org",
      gradeClass: 12,
      avatarId: "panda",
      themeColorKey: "sage",
      preferredLanguage: "English",
      parentalConsentGiven: true,
      centerName: "Asha Kiran Kendra",
      pinSequence: "1,1,2,2",
      xpPoints: 0,
      modulesCompleted: 0,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated owner: doubts create, educator reply, and terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("doubts").doc("doubt_1").set({
      userId: ALICE_UID,
      studentProfileId: "prof_1",
      studentName: "Asha",
      gradeClass: 5,
      subject: "Science",
      questionText: "How do plants make food using sunlight?",
      status: "synced",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("doubts").doc("doubt_1").update({
      status: "answered",
      educatorReply: "Plants use chlorophyll and sunlight in photosynthesis!",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: once answered, cannot be mutated again
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("doubts").doc("doubt_1").update({
      status: "pending",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Authenticated owner: uploaded_lessons create, quality review update, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("educators").doc("edu_1").set({
      userId: ALICE_UID,
      fullName: "Priya Sharma",
      email: "priya.teacher@twogether.org",
      tier: "TIER_1_PAID",
      board12th: "CBSE",
      rollYear: "2022",
      stream: "Science",
      subjectSpecialty: "Mathematics",
      referenceContact: "Dr. Sunita Verma",
      documentAttached: true,
      docVerificationStatus: "AI_VERIFIED",
      docAiSummary: "Valid CBSE Class XII certificate verified.",
      docConfidence: 96,
      isBackupRoster: true,
      adminApproved: true,
      testScore: 3,
      contractSigned: true,
      trainingCompleted: true,
      onboardingStep: 5,
      hoursTaught: 14.5,
      doubtsResolved: 6,
      studentsMentored: 18,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("uploaded_lessons").doc("lesson_u1").set({
      userId: ALICE_UID,
      title: "Decimals in Daily Market Money",
      subject: "Math",
      gradeClass: 5,
      durationLabel: "7 min",
      summary: "Learn rupees and paise using decimal dots.",
      studyNotes: "1 Rupee = 100 Paise. 0.50 = 50 paise.",
      language: "Bilingual",
      uploadedByEducatorEmail: "priya.teacher@twogether.org",
      uploadedByEducatorName: "Priya Sharma",
      qualityStatus: "APPROVED",
      feedbackRating: 5.0,
      feedbackCount: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("educators").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("student_profiles").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("doubts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("live_sessions").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("uploaded_lessons").get());
});
