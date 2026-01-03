# MySQL Database Setup Guide for XAMPP

This guide will help you set up the MySQL database for the Social Media application using XAMPP.

## Prerequisites

- XAMPP installed on your computer
- Basic familiarity with phpMyAdmin or MySQL command line

## Step-by-Step Setup Instructions

### Method 1: Using phpMyAdmin (Recommended for Beginners)

1. **Start XAMPP**
   - Open XAMPP Control Panel
   - Start the **Apache** module
   - Start the **MySQL** module
   - Both should show green "Running" status

2. **Open phpMyAdmin**
   - Click the **Admin** button next to MySQL in XAMPP Control Panel
   - Or open your browser and go to: `http://localhost/phpmyadmin`

3. **Import the Database Schema**
   - Click on the **Import** tab at the top
   - Click **Choose File** button
   - Navigate to your project folder: `C:\Users\Lenovo\Desktop\SMD-Codes\sociallypersonal\`
   - Select the file: `database_schema.sql`
   - Scroll down and click **Go** button
   - Wait for the import to complete

4. **Verify Database Creation**
   - Click on **Databases** in the left sidebar
   - You should see `sociallypersonal` in the list
   - Click on `sociallypersonal` to expand it
   - You should see 11 tables:
     - users
     - posts
     - stories
     - story_chunks
     - comments
     - likes
     - followers
     - following
     - sessions
     - messages
     - user_tokens

5. **Enable Event Scheduler (Optional but Recommended)**
   - Click on the **SQL** tab at the top
   - Enter this command:
     ```sql
     SET GLOBAL event_scheduler = ON;
     ```
   - Click **Go**
   - This enables automatic cleanup of old stories (older than 24 hours)

### Method 2: Using MySQL Command Line

1. **Start XAMPP**
   - Start Apache and MySQL modules

2. **Open Command Prompt**
   - Press `Win + R`
   - Type `cmd` and press Enter

3. **Navigate to MySQL bin directory**
   ```bash
   cd C:\xampp\mysql\bin
   ```

4. **Login to MySQL**
   ```bash
   mysql -u root -p
   ```
   - Press Enter (default XAMPP has no password)
   - If you set a password, enter it when prompted

5. **Import the Schema**
   ```bash
   source C:\Users\Lenovo\Desktop\SMD-Codes\sociallypersonal\database_schema.sql
   ```

6. **Verify Database**
   ```sql
   SHOW DATABASES;
   USE sociallypersonal;
   SHOW TABLES;
   ```

7. **Enable Event Scheduler**
   ```sql
   SET GLOBAL event_scheduler = ON;
   ```

8. **Exit MySQL**
   ```sql
   EXIT;
   ```

## Database Configuration

### Default MySQL Credentials (XAMPP)
- **Host**: `localhost` or `127.0.0.1`
- **Port**: `3306`
- **Username**: `root`
- **Password**: (empty by default)
- **Database**: `sociallypersonal`

### For Production (Security Recommendations)

If deploying to a production server, you should:

1. **Create a dedicated database user**
   ```sql
   CREATE USER 'socialmedia_user'@'localhost' IDENTIFIED BY 'your_strong_password';
   GRANT ALL PRIVILEGES ON sociallypersonal.* TO 'socialmedia_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Set a root password**
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_root_password';
   ```

## Testing the Database

### Test with Sample Data

The schema includes a sample user for testing:
- **Email**: `test@example.com`
- **Username**: `testuser`
- **Password**: `password123`

You can use this to test the login functionality.

### Verify Tables

Run these queries in phpMyAdmin SQL tab to verify:

```sql
-- Check if tables exist
SELECT COUNT(*) as table_count 
FROM information_schema.tables 
WHERE table_schema = 'sociallypersonal';
-- Should return 11

-- Check sample user
SELECT id, email, username, first_name, last_name 
FROM users;
-- Should show the test user

-- Check foreign keys
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'sociallypersonal'
AND REFERENCED_TABLE_NAME IS NOT NULL;
-- Should show all foreign key relationships
```

## Common Issues and Solutions

### Issue 1: "Table already exists" error
**Solution**: The database was already created. Either:
- Drop the existing database first:
  ```sql
  DROP DATABASE IF EXISTS sociallypersonal;
  ```
  Then re-import the schema
- Or skip the import if the database is already set up correctly

### Issue 2: "Access denied" error
**Solution**: 
- Make sure MySQL is running in XAMPP
- Check if you're using the correct username (default is `root`)
- Check if you've set a password (default XAMPP has no password)

### Issue 3: Event scheduler not working
**Solution**:
- Enable event scheduler: `SET GLOBAL event_scheduler = ON;`
- To make it permanent, edit `C:\xampp\mysql\bin\my.ini`
- Add under `[mysqld]` section: `event_scheduler = ON`
- Restart MySQL in XAMPP

### Issue 4: Import fails with "max_allowed_packet" error
**Solution**:
- Edit `C:\xampp\mysql\bin\my.ini`
- Find `max_allowed_packet` and increase it:
  ```ini
  max_allowed_packet = 64M
  ```
- Restart MySQL in XAMPP

### Issue 5: Cannot connect from PHP
**Solution**:
- Verify MySQL is running
- Check connection details in your PHP config file
- Make sure you're using `localhost` or `127.0.0.1`
- Port should be `3306`

## Database Maintenance

### Backup Database

**Using phpMyAdmin**:
1. Select `sociallypersonal` database
2. Click **Export** tab
3. Choose **Quick** export method
4. Format: **SQL**
5. Click **Go**
6. Save the `.sql` file

**Using Command Line**:
```bash
cd C:\xampp\mysql\bin
mysqldump -u root sociallypersonal > backup.sql
```

### Restore Database

**Using phpMyAdmin**:
1. Drop existing database (if needed)
2. Create new database
3. Import the backup `.sql` file

**Using Command Line**:
```bash
cd C:\xampp\mysql\bin
mysql -u root sociallypersonal < backup.sql
```

### Clean Old Data

The database includes an automatic cleanup procedure for stories older than 24 hours. To manually run it:

```sql
CALL cleanup_old_stories();
```

### Monitor Database Size

```sql
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'sociallypersonal'
ORDER BY (data_length + index_length) DESC;
```

## Next Steps

After setting up the database:

1. ✅ Database is created and ready
2. ⏭️ Deploy PHP API files to `C:\xampp\htdocs\sociallyphps\`
3. ⏭️ Configure database connection in PHP `config.php`
4. ⏭️ Test API endpoints
5. ⏭️ Update Android app with server IP address
6. ⏭️ Build and test the Android app

## Support

If you encounter any issues:
- Check XAMPP error logs: `C:\xampp\mysql\data\mysql_error.log`
- Verify MySQL is running in XAMPP Control Panel
- Ensure no other application is using port 3306
- Try restarting XAMPP completely

---

**Database Schema Version**: 1.0  
**Last Updated**: 2025-11-25  
**Compatible with**: XAMPP 8.x, MySQL 8.x
