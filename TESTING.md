# Verification record — 25 September 2026

The existing backend and database were preserved. Before parser changes, the original PDF returned HTTP 200 with correct education, internship experience and technical skills. Resume upload subsequently returned HTTP 201 and was retrieved from PostgreSQL.

## Automated checks

- Seven JUnit tests passed through the JUnit Platform launcher: missing-skill scoring, skill boundaries, unknown experience, score bounds, letter-spaced headings, common headings and body-text boundaries.
- The React production build passed with Vite's native config loader.
- All Java application sources compiled successfully through the Java compiler API.
- The runnable JAR passed 23 checks against PostgreSQL: database health, bundled frontend, candidate/recruiter login, password exclusion, unauthenticated access, candidate ownership, recruiter access, score calculation, PDF regression, DOCX parsing, file type/size/empty validation, history persistence, company/job relationships, recommendations and logout.

## Browser workflows checked

- Candidate login and dashboard with persisted scores/history.
- PDF upload, parsed fields, and saved resume retrieval.
- DOCX upload with skills, both internships, and education displayed.
- Job recommendation details showing score, matched/missing skills and experience.
- Saved Spring Boot milestone appeared in history and changed the demo Java role match from 73 to 100.
- Recruiter login, filtered candidate search, and candidate profile/resume/history.
- Responsive layout observed at a narrow desktop panel width and full desktop width.

## Limits

The standard Gradle build failed with Windows sandbox `AccessDeniedException` while closing dependency archives. Direct Java compilation and the JUnit launcher succeeded; the supplied JAR was packaged from the existing Spring Boot runtime dependencies and the newly compiled sources, then tested directly. It is not represented as a Gradle-produced artifact.

The Docker build and GitHub Actions workflow have been prepared but not executed. No public deployment or production end-to-end test has occurred. GitHub and hosting account access are needed to perform those steps.

The supplied JAR contains the frontend and no local database credentials. Runtime database access must be configured separately. Local demo users, uploaded resume text and sample companies/jobs exist only in the local PostgreSQL database, not in the source package.
