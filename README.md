# User Role: IT Operations Supervisor
**Student ID:** IT25102086
**Portal Routes:** `/portal/it`, `/portal/it/members`

## What this role does in the system
The IT Operations Supervisor is the system administrator — this role is the only one
with unrestricted access, and it controls who can access what across every other portal.

## Access
- Assigned role: `IT_ADMIN` — this role automatically passes every other portal's access
  check in the system, so it can also view/use the Manager, Hospital, QA, Delivery and
  Recipient portals if needed for support purposes.

## What happens on this portal
1. **View all user accounts** — a full list of every registered user in the system
   (donors, staff, hospital coordinators, etc.) with their role and status.
2. **Create new user accounts** — set username, password, full name, role, associated
   hospital (if relevant), and an initial custom permission set (or leave it at the
   role's default permissions).
3. **Manage role-level default permissions** — change what each *role* (Recipient,
   Hospital Coordinator, QA Officer, Delivery Officer, Blood Bank Manager) is allowed to
   do by default, and which portals each role can access.
4. **Manage individual user permissions** — override a specific user's permissions
   beyond their role default, or reset them back to the role default.
5. **Enable / disable accounts** — toggle any user's account between Active and
   Disabled without deleting it.
6. **Delete user accounts** — permanently remove a user from the system.
7. **View audit logs** — a running, timestamped log of security-relevant actions across
   the system (logins, permission changes, record edits) for accountability.

## What this role cannot do
- Nothing is technically blocked (IT_ADMIN bypasses role checks), but in normal
  day-to-day operation this role is expected to focus on account/permission management,
  authentication, and system security/backups rather than doing other roles' clinical or
  logistics work.
