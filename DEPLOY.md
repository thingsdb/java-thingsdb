1. Install packages:
```bash
sudo apt install default-jdk maven


2. Update dependencies:
```bash
mvn versions:display-dependency-updates
```

3. Test:
```bash
mvn clean test
```

## Step 4.. If a new key is required

4. Generate key
```bash
gpg --full-generate-key
> (1) RSA and RSA
> Size: 4096
> 0 = key does not expire
> name (Jeroen van der Heijden)
> email (jeroen@cesbit.com)
> enter PASSPHRASE
```

5. Upload key
```bash
gpg --list-keys --keyid-format LONG
```

Handle Output:
```
pub   rsa4096/0123456789ABCDEF 2026-06-19 [SC]
        // put as <keyname>0x0123456789ABCDEF</keyname> in pom.xml
      XXXXXXXXXXXXXXXXXXXXXXXX0123456789ABCDEF
        // gpg --keyserver keys.openpgp.org --send-keys XXXXXXXXXXXXXXXXXXXXXXXX0123456789ABCDEF
        // gpg --keyserver keyserver.ubuntu.com --send-keys XXXXXXXXXXXXXXXXXXXXXXXX0123456789ABCDEF

```

Wait +/- 5 min:

6. Deploy:
```bash
551  mvn clean deploy -Dgpg.passphrase="<PASSPHRASE>"
```

