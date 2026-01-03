# Complete Deployment Guide

## 🚀 Quick Start - Get Running in 30 Minutes

This guide will get your migrated app running from scratch.

---

## Step 1: Setup MySQL Database (5 minutes)

### 1.1 Start XAMPP
1. Open XAMPP Control Panel
2. Click **Start** for Apache
3. Click **Start** for MySQL
4. Both should show green "Running" status

### 1.2 Import Database
1. Click **Admin** next to MySQL (opens phpMyAdmin)
2. Click **Import** tab
3. Click **Choose File**
4. Select: `C:\Users\Lenovo\Desktop\SMD-Codes\sociallypersonal\database_schema.sql`
5. Click **Go** at the bottom
6. Wait for "Import has been successfully finished"

### 1.3 Verify Database
1. Click **Databases** in left sidebar
2. You should see `sociallypersonal`
3. Click on it - you should see 11 tables
4. Click on `users` table - you should see 1 test user

**Test User Credentials:**
- Email: test@example.com
- Username: testuser
- Password: password123

---

## Step 2: Deploy PHP APIs (5 minutes)

### 2.1 Copy Files
1. Open File Explorer
2. Navigate to: `C:\Users\Lenovo\Desktop\SMD-Codes\sociallypersonal\sociallyphps\`
3. Copy the entire `sociallyphps` folder
4. Paste into: `C:\xampp\htdocs\`
5. Final path should be: `C:\xampp\htdocs\sociallyphps\`

### 2.2 Verify Deployment
1. Open browser
2. Go to: `http://localhost/sociallyphps/config.php`
3. You should see a blank page or JSON headers (no errors)
4. If you see errors, check that Apache and MySQL are running

### 2.3 Test API
Open browser and go to:
```
http://localhost/sociallyphps/get_user.php?username=testuser
```

You should see JSON response:
```json
{
  "success": true,
  "message": "User found",
  "user": {
    "id": 1,
    "email": "test@example.com",
    "username": "testuser",
    ...
  }
}
```

---

## Step 3: Configure Android App (5 minutes)

### 3.1 Find Your IP Address

**On Windows:**
1. Open Command Prompt
2. Type: `ipconfig`
3. Look for "IPv4 Address" under your active network
4. Example: `192.168.1.100`

**Important:** Use your actual IP, NOT `localhost` or `127.0.0.1`

### 3.2 Update API Configuration
1. Open Android Studio
2. Open file: `ApiConfig.kt`
3. Find line: `private const val BASE_IP = "192.168.1.100"`
4. Replace `192.168.1.100` with YOUR IP address
5. Save the file

### 3.3 Sync Gradle
1. In Android Studio, click **File** → **Sync Project with Gradle Files**
2. Wait for sync to complete (may take 2-3 minutes)
3. Check for errors in Build output

---

## Step 4: Build and Run (10 minutes)

### 4.1 Clean Build
1. In Android Studio: **Build** → **Clean Project**
2. Wait for completion
3. Then: **Build** → **Rebuild Project**
4. Wait for build to finish

### 4.2 Run on Device/Emulator
1. Connect Android device via USB OR start emulator
2. Click **Run** button (green play icon)
3. Select your device
4. Wait for app to install and launch

---

## Step 5: Test the App (5 minutes)

### 5.1 Test Login
1. Open the app
2. Click **Login**
3. Enter:
   - Username: `testuser`
   - Password: `password123`
4. Click **Login**
5. Should navigate to home screen

### 5.2 Test Registration
1. Click **Register**
2. Fill in all fields with new user data
3. Click **Register**
4. Should create account and login

### 5.3 Test Offline Mode
1. Turn off WiFi on your device
2. Create a new post
3. Should see "Post saved (will sync when online)"
4. Turn WiFi back on
5. Post should automatically sync to server

---

## Troubleshooting

### Problem: "Database connection failed"
**Solution:**
- Check MySQL is running in XAMPP
- Verify database name is `sociallypersonal`
- Check `config.php` has correct credentials

### Problem: "Network error" in Android app
**Solution:**
- Verify your phone and computer are on same WiFi network
- Check IP address in `ApiConfig.kt` is correct
- Test API in browser: `http://YOUR_IP/sociallyphps/get_user.php?username=testuser`
- Check Windows Firewall isn't blocking port 80

### Problem: "404 Not Found" for APIs
**Solution:**
- Verify files are in `C:\xampp\htdocs\sociallyphps\`
- Check Apache is running
- Test: `http://localhost/sociallyphps/config.php`

### Problem: Gradle sync fails
**Solution:**
- Check internet connection
- Click **File** → **Invalidate Caches** → **Invalidate and Restart**
- Delete `.gradle` folder in project root
- Sync again

### Problem: App crashes on launch
**Solution:**
- Check Logcat in Android Studio for errors
- Verify all new files (ApiClient.kt, LocalDatabaseHelper.kt, etc.) are in correct package
- Clean and rebuild project

---

## Testing Checklist

### Basic Functionality
- [ ] Login with test user
- [ ] Register new user
- [ ] View feed/home screen
- [ ] Create new post
- [ ] Like/unlike posts
- [ ] Add comments
- [ ] View user profile
- [ ] Follow/unfollow users

### Offline Functionality
- [ ] Create post while offline
- [ ] Post syncs when back online
- [ ] View cached posts while offline
- [ ] Like/comment queued while offline
- [ ] Sync queue processes when online

### Performance
- [ ] App loads quickly
- [ ] Images load smoothly
- [ ] No crashes or freezes
- [ ] Sync happens in background

---

## API Testing with Postman

### Install Postman
1. Download from: https://www.postman.com/downloads/
2. Install and open

### Test Registration
1. Create new request
2. Method: **POST**
3. URL: `http://YOUR_IP/sociallyphps/register.php`
4. Body → raw → JSON:
```json
{
  "email": "newuser@test.com",
  "password": "password123",
  "username": "newuser",
  "firstName": "New",
  "lastName": "User",
  "dob": "01-01-2000",
  "profilePicBase64": "data:image/png;base64,iVBORw0KG...",
  "deviceId": "test_device"
}
```
5. Click **Send**
6. Should get success response

### Test Login
1. Method: **POST**
2. URL: `http://YOUR_IP/sociallyphps/login.php`
3. Body:
```json
{
  "username": "testuser",
  "password": "password123"
}
```
4. Should return user data

### Test Get Posts
1. Method: **GET**
2. URL: `http://YOUR_IP/sociallyphps/get_posts.php?userId=1`
3. Should return posts array

---

## Network Security Config (If needed)

If you get network security errors, create this file:

**File:** `app/src/main/res/xml/network_security_config.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**Then add to AndroidManifest.xml:**
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

## Production Deployment

### For Production Server:

1. **Get a domain and SSL certificate**
2. **Update config.php:**
   ```php
   define('DB_HOST', 'your_production_host');
   define('DB_USER', 'your_db_user');
   define('DB_PASS', 'strong_password');
   ```

3. **Update ApiConfig.kt:**
   ```kotlin
   const val BASE_URL = "https://yourdomain.com/sociallyphps/"
   ```

4. **Enable HTTPS only**
5. **Add authentication tokens (JWT)**
6. **Set up proper error logging**
7. **Configure rate limiting**
8. **Set up automated backups**

---

## Performance Optimization

### Database
- Indexes already created in schema
- Run cleanup procedure weekly:
  ```sql
  CALL cleanup_old_stories();
  ```

### Android App
- Images are cached locally
- Sync runs in background
- WorkManager handles periodic sync

### Server
- Enable gzip compression in Apache
- Set up caching headers
- Consider CDN for media files

---

## Monitoring

### Check API Logs
Location: `C:\xampp\htdocs\sociallyphps\logs\api_requests.log`

### Check MySQL Logs
Location: `C:\xampp\mysql\data\mysql_error.log`

### Check Apache Logs
Location: `C:\xampp\apache\logs\error.log`

---

## Backup Strategy

### Database Backup (Daily)
```bash
cd C:\xampp\mysql\bin
mysqldump -u root sociallypersonal > backup_YYYYMMDD.sql
```

### Restore Database
```bash
mysql -u root sociallypersonal < backup_YYYYMMDD.sql
```

---

## Support

### Common Commands

**Restart Apache:**
- XAMPP Control Panel → Stop Apache → Start Apache

**Restart MySQL:**
- XAMPP Control Panel → Stop MySQL → Start MySQL

**Clear Android App Data:**
- Settings → Apps → Your App → Storage → Clear Data

**Rebuild Android App:**
- Build → Clean Project → Rebuild Project

---

## Success Criteria

Your migration is successful when:
- ✅ All 29 PHP APIs respond correctly
- ✅ Login/Registration works
- ✅ Posts can be created and viewed
- ✅ Offline mode works (create post offline, syncs when online)
- ✅ No Firebase dependencies remain
- ✅ App performs as well or better than Firebase version

---

## Next Steps After Deployment

1. **Test all features thoroughly**
2. **Migrate existing Firebase data** (if needed)
3. **Set up production server**
4. **Configure SSL/HTTPS**
5. **Implement push notifications** (if needed)
6. **Add analytics** (if needed)
7. **Set up monitoring and alerts**

---

**Deployment Version:** 1.0  
**Last Updated:** 2025-11-25  
**Estimated Setup Time:** 30 minutes  
**Difficulty:** Intermediate
