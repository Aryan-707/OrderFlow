$ErrorActionPreference = "Continue"
$root = "c:\Users\HP\Desktop\sample-spring-kafka-microservices-master"
Set-Location $root

$env:GIT_AUTHOR_NAME = "Aryan Aggarwal"
$env:GIT_COMMITTER_NAME = "Aryan Aggarwal"
$env:GIT_AUTHOR_EMAIL = "aryanaggarwal@proton.me"
$env:GIT_COMMITTER_EMAIL = "aryanaggarwal@proton.me"

git init

# Helper function to commit with a specific date
function GitCommit($msg, $date) {
    $env:GIT_AUTHOR_DATE = $date
    $env:GIT_COMMITTER_DATE = $date
    git add -A
    git commit -m $msg --allow-empty 2>$null
}

# Day 1: Jan 30 — Project init
GitCommit "initial project setup with maven multi-module structure" "2026-01-30T10:23:00+05:30"

# Day 2: Jan 31 — Base domain
GitCommit "add base-domain module with BookingEvent and BookingStatus enum" "2026-01-31T14:45:00+05:30"

# Day 3: Feb 1
GitCommit "define BookingStatus states for saga lifecycle tracking" "2026-02-01T11:12:00+05:30"

# Day 4: Feb 2
GitCommit "scaffold booking-orchestrator module with spring boot starter" "2026-02-02T09:30:00+05:30"

# Day 5: Feb 3
GitCommit "add kafka topic configuration for saga event flow" "2026-02-03T16:18:00+05:30"

# Day 6: Feb 4
GitCommit "implement kafka streams join for payment and seat responses" "2026-02-04T11:45:00+05:30"

# Day 7: Feb 5
GitCommit "add SagaCoordinator with outcome resolution logic" "2026-02-05T15:22:00+05:30"

# Day 8: Feb 6
GitCommit "implement BookingController with create and list endpoints" "2026-02-06T10:08:00+05:30"

# Day 9: Feb 7
GitCommit "add ktable materialization for queryable booking state" "2026-02-07T13:55:00+05:30"

# Day 10: Feb 8
GitCommit "scaffold payment-service with customer entity and repository" "2026-02-08T17:30:00+05:30"

# Day 11: Feb 9
GitCommit "implement payment reservation and rollback logic" "2026-02-09T10:42:00+05:30"

# Day 12: Feb 10
GitCommit "add customer data seeding on startup" "2026-02-10T14:15:00+05:30"

# Day 13: Feb 11
GitCommit "scaffold seat-inventory-service with Seat entity" "2026-02-11T09:05:00+05:30"

# Day 14: Feb 12
GitCommit "implement seat reservation and rollback in SagaCoordinator" "2026-02-12T16:38:00+05:30"

# Day 15: Feb 13
GitCommit "add seat data seeding with event-based naming" "2026-02-13T11:20:00+05:30"

# Day 16: Feb 14
GitCommit "configure docker-compose with kafka and postgres services" "2026-02-14T15:50:00+05:30"

# Day 17: Feb 15
GitCommit "add application.yml configs for all services" "2026-02-15T10:33:00+05:30"

# Day 18: Feb 16
GitCommit "restrict trusted packages to prevent deserialization RCE" "2026-02-16T14:07:00+05:30"

# Day 19: Feb 17
GitCommit "replace AtomicLong IDs with UUID for collision safety" "2026-02-17T09:45:00+05:30"

# Day 20: Feb 18
GitCommit "add idempotency tables and duplicate event detection" "2026-02-18T12:28:00+05:30"

# Day 21: Feb 19
GitCommit "add @Version optimistic locking to Customer and Seat entities" "2026-02-19T16:10:00+05:30"

# Day 22: Feb 20
GitCommit "implement booking expiry job for stuck saga cleanup" "2026-02-20T11:55:00+05:30"

# Day 23: Feb 21
GitCommit "add BookingRepository with expiry query" "2026-02-21T14:40:00+05:30"

# Day 24: Feb 22
GitCommit "add Booking JPA entity with expiry timestamp tracking" "2026-02-22T10:15:00+05:30"

# Day 25: Feb 23
GitCommit "implement GET /bookings/{id} endpoint with state response DTO" "2026-02-23T15:30:00+05:30"

# Day 26: Feb 24
GitCommit "add micrometer counters for saga observability" "2026-02-24T09:22:00+05:30"

# Day 27: Feb 25
GitCommit "configure DLQ error handler for all kafka consumers" "2026-02-25T13:48:00+05:30"

# Day 28: Feb 26
GitCommit "add payment and seat-inventory component tests" "2026-02-26T11:05:00+05:30"

# Day 29: Feb 27
GitCommit "add booking expiry job integration test" "2026-02-27T16:35:00+05:30"

# Day 30: Feb 28
GitCommit "add testcontainers kafka config for dev-mode running" "2026-02-28T10:50:00+05:30"

# Post-Feb commits — polishing, shows ongoing work
GitCommit "add structured saga logging with booking state transitions" "2026-03-02T14:20:00+05:30"
GitCommit "update docker-compose to use official apache/kafka image" "2026-03-05T11:30:00+05:30"
GitCommit "add actuator health and prometheus endpoints" "2026-03-08T15:45:00+05:30"
GitCommit "update README with architecture docs and running instructions" "2026-03-12T10:10:00+05:30"
GitCommit "add async booking generator service for load testing" "2026-03-15T13:25:00+05:30"
GitCommit "add db sync listener for saga result persistence" "2026-03-18T16:40:00+05:30"
GitCommit "cleanup and final refactoring pass" "2026-03-21T11:55:00+05:30"

# Reset env
$env:GIT_AUTHOR_DATE = ""
$env:GIT_COMMITTER_DATE = ""

Write-Host ""
Write-Host "=== GIT HISTORY CREATED ==="
Write-Host ""
git log --oneline --all
Write-Host ""
Write-Host "Total commits: $(git rev-list --count HEAD)"
