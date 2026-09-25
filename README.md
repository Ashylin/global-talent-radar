# Global Talent Radar

A college-demo talent intelligence application, continued from the existing Java 21 / Spring Boot 4.2.0-SNAPSHOT / PostgreSQL 17 project. The React/Tailwind frontend lives in `frontend/`.

## Run locally

Requirements: Java 21, Node 24, PostgreSQL 17. The Gradle wrapper is included.

1. Keep the existing `global_talent_radar` database. Set `DATABASE_URL` to a JDBC URL, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`, or use the ignored `application-local.properties` for your existing local database connection.
2. In `frontend/`, run `npm ci` then `npm run build`.
3. In the project root, run `gradlew.bat test bootJar` (Windows), or `./gradlew test bootJar` (Linux/macOS).
4. Run `java -jar build/libs/global-talent-radar.jar` from the project root.
5. Open **http://localhost:8080/app**. The original `/` backend test endpoint remains available. Database health is at `/api/health`.

On Windows, `./build-all.ps1` performs steps 2–3. To include the existing PostgreSQL application-context test, set `RUN_DATABASE_TESTS=true` before running Gradle. Unit tests otherwise run without a database.

For frontend development: `npm run dev` in `frontend/` proxies `/api` to port 8080. `API_PROXY_TARGET` can override the proxy target. `npm run preview -- --configLoader native` previews a production build. `VITE_API_URL` is only needed if the frontend is hosted on a separate origin; set it to the backend origin without `/api`, and configure `CORS_ORIGINS` on the backend.

## Demonstration flow

1. Create a candidate account. Upload a PDF or DOCX (maximum 10 MB).
2. Review the extracted education, experience and skills. The original document binary is not retained; parsed text and fields are saved to PostgreSQL.
3. Open job recommendations, then a job's match details. A profile without a resume can also match using manually recorded skills.
4. Add a skill milestone. Skill profiles and performance entries persist, and recommendations refresh.
5. Create/sign in to a recruiter account. Search candidates by job, skills, country, region, experience, match score and readiness. Open candidate profiles.
6. Under Manage jobs, create a company and publish a role. Newly published roles are available for matching.

Empty states represent real empty data; there are no fabricated dashboard metrics. A fresh production database starts empty. Register accounts and create a company/job to start the demo. Local test records are not shipped or uploaded to production.

## Matching rules

Score = 80% required-skill coverage + 20% experience coverage, capped at 100. Skills are case-insensitive whole-token/phrase matches with a small alias normalization (e.g. SpringBoot). JavaScript does not imply Java, and C++ does not imply C. Required skills are comma/semicolon/newline separated; duplicates count once.

Candidate experience is the maximum explicit `N years` statement in the experience section or a recorded skill's years of experience, not the sum of skill years. Date-only internships are reported as unknown; the app does not infer durations. The recorded skill experience is a simple demo proxy for overall experience, not verified employment history. Job experience should be entered as a number of years; zero means no experience required.

- READY TO APPLY: all required skills and the experience requirement are met.
- NEEDS 1 SKILL: exactly one required skill is missing and experience is sufficient.
- UNDERQUALIFIED: two or more skills are missing, or required experience is insufficient/unknown.

Missing job-description keywords are informational and do not affect the score. Location filters never affect scoring. These deterministic scores describe profile/job keyword fit, not hiring predictions.

## Authentication and demo limits

Passwords are BCrypt-hashed and excluded from JSON, including nested user objects. Existing plaintext passwords are hashed on backend startup without changing the user's password. Bearer sessions last eight hours, live in server memory, and are invalidated on restart. The browser stores its token in sessionStorage. Candidates can modify only their own data; recruiters can read candidate profiles and create companies/jobs.

This is a single-instance college demo: recruiter self-registration is open, so any registered recruiter can view the candidate pool. Before opening a real recruitment service, add recruiter approval, pagination, persistent sessions, rate limiting, retention/consent controls, and managed schema migrations. Only use HTTPS when deployed. Do not use real applicant records in a public demo.

## Deployment

The multi-stage Dockerfile builds React and Java and serves both on one origin. The Java backend remains the application's backend; PostgreSQL remains its database. `render.yaml` defines a Docker web service plus PostgreSQL 17 and supplies database credentials through environment variables.

1. Push this source (excluding local configuration, build outputs and node_modules) to a GitHub repository.
2. In Render, create a Blueprint from that repository and review the two free resources before deployment.
3. Open the resulting service URL with `/app` appended. Confirm `/api/health` reports `UP`.
4. Register fresh demo accounts and repeat upload → match → progression → recruiter search.

The configured free Render web service sleeps when idle, and the free database has an expiry period. Review current [Render free-tier limits](https://render.com/docs/free) before relying on it for a scheduled presentation. A paid plan must be selected explicitly by the account owner if required. No hosting resources are created by the source files themselves.

Native production environment:

```
DATABASE_URL=jdbc:postgresql://host:5432/global_talent_radar?sslmode=require
DATABASE_USERNAME=your_user
DATABASE_PASSWORD=your_secret
PORT=8080
```

The Render Blueprint provides its PostgreSQL connection string as `DATABASE_URL`; the backend converts `postgresql://user:password@host/database` URLs to JDBC configuration. Same-origin deployments need no `VITE_API_URL`. For separate hosting, build the frontend with `VITE_API_URL=https://your-backend.example` and set `CORS_ORIGINS=https://your-frontend.example`.

For Docker locally, copy `.env.example` to `.env`, choose a password, then run `docker compose up --build`. PostgreSQL data uses a named volume. Do not delete that volume unless you intend to erase the demo database.

## Tests and known build limitation

`MatchEngineTests` and `ResumeSectionsTests` cover scoring, missing skills, skill boundaries, unknown experience, letter-spaced headings and common resume layouts. The existing context test can be enabled with `RUN_DATABASE_TESTS=true` and an accessible PostgreSQL database.

In the Codex Windows sandbox used for development, Gradle's Java archive cleanup produced `AccessDeniedException` even when dependency JARs were readable. Compilation and seven JUnit tests were therefore also verified through the Java compiler/JUnit launcher directly. This does not constitute a successful normal Gradle/Docker build in that environment. Run the provided build pipeline in an ordinary terminal or CI to verify that route. The supplied runnable JAR is separately smoke-tested.

## API overview

- POST `/api/auth/register`, `/api/auth/login`; GET `/api/auth/me`; POST `/api/auth/logout`
- POST `/api/resumes/parse` (multipart `file`), POST `/api/resumes/upload` (`file`, `userId`)
- POST `/api/match/calculate`: use `{ "userId": 1, "jobId": 1 }`, or raw candidate skills/experience and required skills
- GET `/api/candidates/{id}/profile`, `/recommendations`, `/history`
- GET `/api/candidates?jobId=1&skills=Java&region=Asia&minScore=70` (recruiter)
- Existing GET/POST `/api/users`, `/api/companies`, `/api/jobs`, `/api/resumes`, `/api/skills`, `/api/performance` remain, with validation and access controls

Except registration/login and health, API requests require `Authorization: Bearer <token>`. Invalid requests return useful JSON messages and HTTP 400/401/403/404/409/413/415/422 as appropriate.
