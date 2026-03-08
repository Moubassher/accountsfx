
<h2> Introduction </h2>
Well, first there has to exist at least 3 types of accounts in the system:

1. "Regular accounts" - with a set of features accessible to users with such accounts.
2. Admin accounts - more privileges.
3. Maintainer accounts: These are for me and whoever I decide to have help me run some 
maintenances tasks in the future inshallah.

- Every system has to have a maintainer account built-in. With access to maintenance-related features, which I decide for each client respectively, in the client programs' codebase.
- A maintainer account is created before anything else, including an admin account.
  A maintainer account can create ACCOUNT TYPES and assign access to features for each
  type of account. The feature list is an enum provided by the dependent project.
- Then, an admin account has to be created first before non-admin accounts can be created.
- After it's created, an admin account can then approve of other accounts' creation.
- Logic flow goes like this:
- 
No account types specified -> maintainer account creates 
No admin account -> create-admin-account-popup -> admin account created
-> account(admin) login
OR
-> create-non-admin-account -> send approval request to admin
-> account(admin) login -> approve account.
-> non-admin-account-login.

```mermaid
flowchart TD
    A{account types specified?} 
    A -- No --> A1[Maintainer login]  -->  B0{Success?} --Yes --> B[Maintainer panel] 
    B0 -- No --> B1[Exit system]
    B --> B2["maintainer account:<br/>✔ specifies account types & assigns privileges <br/>✔ creates accounts <br/>✔ approves accounts"] --> C1
    
    
  A -- Yes --> C1{admin accounts exist?} 
  C1 -- No --> D[Admin account required popup] --> E[create admin account] -->  F[Admin login] 
  C1 -- Yes --> C2(Login screen) --> C3(Main App)

  F --> G[Create non-admin account] --> H[Send approval request to admin]
  H --> I["Admin/maintainer login"]
  I --> J[Approve account]
  J --> K[Non-admin account login] --> C3
  
  classDef tested fill: green
 class A,A1,B,B2 tested
  
```

```mermaid
flowchart TD
    A[features] --> B["account types"]
    B --> C["Account"]
    C --> D("Admin")
    C --> E("Non-admin")
```