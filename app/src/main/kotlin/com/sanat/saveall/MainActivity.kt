package com.sanat.saveall

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private val PremiumBlue = Color(0xFF405AC5)
private val PremiumBlueDark = Color(0xFF273B90)
private val PremiumBlueLight = Color(0xFF6C84E8)
private val PageBackground = Color(0xFFF6F7FC)
private val CardBackground = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF252B3B)
private val SuccessGreen = Color(0xFF18766C)
private val DangerRed = Color(0xFFC0393A)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var date: String,
    var description: String,
    var createdAt: Long = System.currentTimeMillis()
)

data class AccountItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var userId: String,
    var password: String,
    var other1: String,
    var other2: String,
    var createdAt: Long = System.currentTimeMillis()
)

data class FileItem(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var storedName: String,
    var originalName: String,
    var mimeType: String,
    var createdAt: Long = System.currentTimeMillis()
)

class AppRepository(private val context: Context) {

    private val prefs =
        context.getSharedPreferences("save_all_preferences", Context.MODE_PRIVATE)

    private val notesKey = "notes"
    private val accountsKey = "accounts"
    private val filesKey = "files"
    private val pinKey = "pin"

    val notes = mutableStateListOf<NoteItem>()
    val accounts = mutableStateListOf<AccountItem>()
    val files = mutableStateListOf<FileItem>()

    init {
        reload()
    }

    fun reload() {
        notes.clear()
        accounts.clear()
        files.clear()

        notes.addAll(readNotes())
        accounts.addAll(readAccounts())
        files.addAll(readFiles())
    }

    fun getPin(): String = prefs.getString(pinKey, "123456") ?: "123456"

    fun setPin(pin: String) {
        prefs.edit().putString(pinKey, pin).apply()
    }

    fun saveNotes() {
        val array = JSONArray()
        notes.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("date", it.date)
                    .put("description", it.description)
                    .put("createdAt", it.createdAt)
            )
        }
        prefs.edit().putString(notesKey, array.toString()).apply()
    }

    fun saveAccounts() {
        val array = JSONArray()
        accounts.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("userId", it.userId)
                    .put("password", it.password)
                    .put("other1", it.other1)
                    .put("other2", it.other2)
                    .put("createdAt", it.createdAt)
            )
        }
        prefs.edit().putString(accountsKey, array.toString()).apply()
    }

    fun saveFiles() {
        val array = JSONArray()
        files.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("storedName", it.storedName)
                    .put("originalName", it.originalName)
                    .put("mimeType", it.mimeType)
                    .put("createdAt", it.createdAt)
            )
        }
        prefs.edit().putString(filesKey, array.toString()).apply()
    }

    fun addNote(item: NoteItem) {
        notes.add(item)
        saveNotes()
    }

    fun addAccount(item: AccountItem) {
        accounts.add(item)
        saveAccounts()
    }

    fun addFile(item: FileItem) {
        files.add(item)
        saveFiles()
    }

    fun deleteNotes(ids: Set<String>) {
        notes.removeAll { it.id in ids }
        saveNotes()
    }

    fun deleteAccounts(ids: Set<String>) {
        accounts.removeAll { it.id in ids }
        saveAccounts()
    }

    fun deleteFiles(ids: Set<String>) {
        val remove = files.filter { it.id in ids }
        remove.forEach {
            File(context.filesDir, "uploads/${it.storedName}").delete()
        }
        files.removeAll { it.id in ids }
        saveFiles()
    }

    private fun readNotes(): List<NoteItem> {
        return try {
            val text = prefs.getString(notesKey, "[]") ?: "[]"
            val array = JSONArray(text)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(
                        NoteItem(
                            id = o.getString("id"),
                            title = o.getString("title"),
                            date = o.getString("date"),
                            description = o.getString("description"),
                            createdAt = o.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readAccounts(): List<AccountItem> {
        return try {
            val text = prefs.getString(accountsKey, "[]") ?: "[]"
            val array = JSONArray(text)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(
                        AccountItem(
                            id = o.getString("id"),
                            name = o.getString("name"),
                            userId = o.getString("userId"),
                            password = o.getString("password"),
                            other1 = o.optString("other1"),
                            other2 = o.optString("other2"),
                            createdAt = o.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readFiles(): List<FileItem> {
        return try {
            val text = prefs.getString(filesKey, "[]") ?: "[]"
            val array = JSONArray(text)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(
                        FileItem(
                            id = o.getString("id"),
                            title = o.getString("title"),
                            storedName = o.getString("storedName"),
                            originalName = o.getString("originalName"),
                            mimeType = o.optString("mimeType", "*/*"),
                            createdAt = o.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun exportJson(): String {
        return JSONObject()
            .put("notes", JSONArray(prefs.getString(notesKey, "[]")))
            .put("accounts", JSONArray(prefs.getString(accountsKey, "[]")))
            .put("files", JSONArray(prefs.getString(filesKey, "[]")))
            .put("pin", getPin())
            .toString()
    }

    fun restoreJson(json: String) {
        val root = JSONObject(json)

        notes.clear()
        accounts.clear()
        files.clear()

        val notesArray = root.optJSONArray("notes") ?: JSONArray()
        val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
        val filesArray = root.optJSONArray("files") ?: JSONArray()

        prefs.edit()
            .putString(notesKey, notesArray.toString())
            .putString(accountsKey, accountsArray.toString())
            .putString(filesKey, filesArray.toString())
            .putString(pinKey, root.optString("pin", "123456"))
            .apply()

        reload()
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    private var selectedUploadUri by mutableStateOf<Uri?>(null)

    private val exportLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/zip")
        ) { uri ->
            if (uri != null) {
                try {
                    exportEverything(uri)
                    Toast.makeText(this, "All data exported successfully", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Export failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private val restoreLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }

                try {
                    restoreEverything(uri)
                    Toast.makeText(this, "All data restored successfully", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Restore failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private val uploadLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            selectedUploadUri = uri

            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = AppRepository(this)

        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = PremiumBlue,
                    secondary = PremiumBlueDark,
                    background = PageBackground,
                    surface = CardBackground
                )
            ) {
                SaveAllApplication()
            }
        }
    }

    private fun exportEverything(uri: Uri) {

        contentResolver.openOutputStream(uri)?.use { output ->
            ZipOutputStream(output).use { zip ->

                zip.putNextEntry(ZipEntry("save_all_data.json"))
                zip.write(repository.exportJson().toByteArray())
                zip.closeEntry()

                repository.files.forEach { item ->
                    val file = File(filesDir, "uploads/${item.storedName}")

                    if (file.exists()) {
                        zip.putNextEntry(
                            ZipEntry("files/${item.storedName}")
                        )

                        FileInputStream(file).use { input ->
                            input.copyTo(zip)
                        }

                        zip.closeEntry()
                    }
                }
            }
        } ?: error("Unable to open destination")
    }

    private fun restoreEverything(uri: Uri) {

        val restoreDir = File(filesDir, "restore_temp")
        restoreDir.deleteRecursively()
        restoreDir.mkdirs()

        var jsonText: String? = null

        contentResolver.openInputStream(uri)?.use { input ->

            ZipInputStream(input).use { zip ->

                var entry = zip.nextEntry

                while (entry != null) {

                    if (!entry.isDirectory) {

                        when {
                            entry.name == "save_all_data.json" -> {
                                jsonText = zip.readBytes().decodeToString()
                            }

                            entry.name.startsWith("files/") -> {
                                val name =
                                    File(entry.name).name.replace(
                                        Regex("[^A-Za-z0-9._-]"),
                                        "_"
                                    )

                                val out = File(restoreDir, name)

                                FileOutputStream(out).use {
                                    zip.copyTo(it)
                                }
                            }
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        val json = jsonText ?: error("Invalid Save All backup")

        val uploads = File(filesDir, "uploads")
        uploads.deleteRecursively()
        uploads.mkdirs()

        restoreDir.listFiles()?.forEach { file ->
            file.copyTo(File(uploads, file.name), overwrite = true)
        }

        restoreDir.deleteRecursively()

        repository.restoreJson(json)
    }

    private fun openFile(item: FileItem) {

        try {
            val file = File(filesDir, "uploads/${item.storedName}")

            if (!file.exists()) {
                Toast.makeText(this, "Saved file not found", Toast.LENGTH_LONG).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                this,
                "com.sanat.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, item.mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Open File"))

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No suitable app found to open this file",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Composable
    private fun SaveAllApplication() {

        var authenticated by rememberSaveable {
            mutableStateOf(false)
        }

        if (!authenticated) {
            PinScreen(
                repository = repository,
                onSuccess = {
                    authenticated = true
                }
            )
        } else {
            MainScreen(
                repository = repository,
                selectedUploadUri = selectedUploadUri,
                onChooseFile = {
                    uploadLauncher.launch(arrayOf("*/*"))
                },
                onClearSelectedFile = {
                    selectedUploadUri = null
                },
                onOpenFile = {
                    openFile(it)
                },
                onExport = {
                    exportLauncher.launch(
                        "SaveAll_Backup_${System.currentTimeMillis()}.zip"
                    )
                },
                onRestore = {
                    restoreLauncher.launch(arrayOf("application/zip", "*/*"))
                },
                onExit = {
                    (this@MainActivity as Activity).finishAndRemoveTask()
                }
            )
        }
    }

    @Composable
    private fun PinScreen(
        repository: AppRepository,
        onSuccess: () -> Unit
    ) {

        var pin by rememberSaveable { mutableStateOf("") }
        var showError by rememberSaveable { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground),
            contentAlignment = Alignment.Center
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {

                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = PremiumBlue
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Save All",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = PremiumBlueDark
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "PIN Authentication",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = TextDark
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "First Time PIN is 123456.",
                        fontSize = 8.sp,
                        color = TextDark
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Use Export All Data before Uninstalling",
                        fontSize = 8.sp,
                        color = Color.DarkGray
                    )

                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6 && it.all(Char::isDigit)) {
                                pin = it
                                showError = false
                            }
                        },
                        label = { Text("Enter 6 Digit PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation =
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError
                    )

                    if (showError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Incorrect PIN",
                            color = DangerRed
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    PremiumButton(
                        text = "UNLOCK SAVE ALL",
                        icon = Icons.Default.Key,
                        onClick = {
                            if (pin == repository.getPin()) {
                                onSuccess()
                            } else {
                                showError = true
                            }
                        }
                    )
                }
            }
        }
    }

    private enum class AppTab(
        val title: String
    ) {
        NOTES("My Notes"),
        ACCOUNTS("My Accounts"),
        FILES("My Files"),
        BACKUP("Export/Restore All"),
        CONTROL("Control Panel")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen(
        repository: AppRepository,
        selectedUploadUri: Uri?,
        onChooseFile: () -> Unit,
        onClearSelectedFile: () -> Unit,
        onOpenFile: (FileItem) -> Unit,
        onExport: () -> Unit,
        onRestore: () -> Unit,
        onExit: () -> Unit
    ) {

        var currentTab by rememberSaveable {
            mutableStateOf(AppTab.NOTES)
        }

        Scaffold(
            containerColor = PageBackground,

            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Save All",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = Color.White
                        )
                    },

                    actions = {
                        Row(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onExit() }
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 8.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Exit",
                                tint = Color.White
                            )

                            Spacer(Modifier.width(4.dp))

                            Text(
                                text = "Exit",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PremiumBlueDark
                    )
                )
            },

            bottomBar = {
                PremiumFooter(
                    currentTab = currentTab,
                    onTabChange = {
                        currentTab = it
                    }
                )
            },

            snackbarHost = {
                SnackbarHost(
                    hostState = remember { SnackbarHostState() }
                )
            }

        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                when (currentTab) {

                    AppTab.NOTES ->
                        NotesPage(repository)

                    AppTab.ACCOUNTS ->
                        AccountsPage(repository)

                    AppTab.FILES ->
                        FilesPage(
                            repository = repository,
                            selectedUploadUri = selectedUploadUri,
                            onChooseFile = onChooseFile,
                            onClearSelectedFile = onClearSelectedFile,
                            onOpenFile = onOpenFile
                        )

                    AppTab.BACKUP ->
                        BackupPage(
                            onExport = onExport,
                            onRestore = onRestore
                        )

                    AppTab.CONTROL ->
                        ControlPanelPage(repository)
                }
            }
        }
    }

    @Composable
    private fun PremiumFooter(
        currentTab: AppTab,
        onTabChange: (AppTab) -> Unit
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PremiumBlueDark)
                .padding(top = 10.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AppTab.entries.forEach { tab ->

                    FooterTab(
                        title = tab.title,
                        selected = currentTab == tab,
                        onClick = {
                            onTabChange(tab)
                        }
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.20f)
            )

            Text(
                text = "@ 2026 Built & Developed by Sanat Dey",
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    @Composable
    private fun FooterTab(
        title: String,
        selected: Boolean,
        onClick: () -> Unit
    ) {

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected)
                        Color.White.copy(alpha = 0.18f)
                    else
                        Color.Transparent
                )
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val icon = when (title) {
                "My Notes" -> Icons.Default.Note
                "My Accounts" -> Icons.Default.AccountBalance
                "My Files" -> Icons.Default.Folder
                "Export/Restore All" -> Icons.Default.Restore
                else -> Icons.Default.Settings
            }

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) Color.White else Color.White.copy(alpha = 0.70f),
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = title,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.75f),
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    @Composable
    private fun PageTitle(
        title: String,
        subtitle: String
    ) {
    
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 10.dp
            )
        ) {
    
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
    
            Spacer(Modifier.height(2.dp))
    
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }

    @Composable
    private fun NotesPage(repository: AppRepository) {

        var showAdd by rememberSaveable {
            mutableStateOf(false)
        }

        var editing by remember {
            mutableStateOf<NoteItem?>(null)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            PageTitle(
                "My Notes",
                "Save and manage your important notes"
            )

            PremiumButton(
                text = "ADD NOTE",
                icon = Icons.Default.Note,
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = {
                    showAdd = true
                }
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Recent Notes",
                modifier = Modifier.padding(horizontal = 20.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextDark
            )

            Spacer(Modifier.height(10.dp))

            repository.notes
                .sortedByDescending { it.createdAt }
                .forEach { note ->

                    NoteCard(
                        note = note,
                        onEdit = {
                            editing = note
                        }
                    )
                }

            if (repository.notes.isEmpty()) {
                EmptyMessage("No notes saved yet")
            }

            Spacer(Modifier.height(20.dp))
        }

        if (showAdd) {
            NoteEditorDialog(
                title = "Add Note",
                note = null,
                onDismiss = {
                    showAdd = false
                },
                onSave = {
                    repository.addNote(it)
                    showAdd = false
                }
            )
        }

        editing?.let { note ->
            NoteEditorDialog(
                title = "Edit Note",
                note = note,
                onDismiss = {
                    editing = null
                },
                onSave = { updated ->
                    note.title = updated.title
                    note.date = updated.date
                    note.description = updated.description
                    repository.saveNotes()
                    editing = null
                }
            )
        }
    }

    @Composable
    private fun NoteCard(
        note: NoteItem,
        onEdit: () -> Unit
    ) {

        var expanded by rememberSaveable(note.id) {
            mutableStateOf(false)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 7.dp
                )
                .clickable {
                    expanded = !expanded
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.Note,
                        contentDescription = null,
                        tint = PremiumBlue,
                        modifier = Modifier.size(34.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )

                        Text(
                            text = note.date,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onEdit
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            tint = PremiumBlue
                        )
                    }
                }

                if (expanded) {

                    Spacer(Modifier.height(14.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(12.dp))

                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = note.description,
                            color = TextDark,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun NoteEditorDialog(
        title: String,
        note: NoteItem?,
        onDismiss: () -> Unit,
        onSave: (NoteItem) -> Unit
    ) {

        var noteTitle by remember {
            mutableStateOf(note?.title ?: "")
        }

        var date by remember {
            mutableStateOf(
                note?.date ?: indianToday()
            )
        }

        var description by remember {
            mutableStateOf(note?.description ?: "")
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(title)
            },
            text = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = {
                            noteTitle = it
                        },
                        label = {
                            Text("Note Title")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = {
                            date = it
                        },
                        label = {
                            Text("Date")
                        },
                        supportingText = {
                            Text("Default: India current date")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        label = {
                            Text("Write Description")
                        },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            onSave(
                                NoteItem(
                                    id = note?.id
                                        ?: UUID.randomUUID().toString(),
                                    title = noteTitle.trim(),
                                    date = date.trim(),
                                    description = description.trim(),
                                    createdAt = note?.createdAt
                                        ?: System.currentTimeMillis()
                                )
                            )
                        }
                    }
                ) {
                    Text("SAVE")
                }
            },

            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    @Composable
    private fun AccountsPage(repository: AppRepository) {

        var showAdd by rememberSaveable {
            mutableStateOf(false)
        }

        var editing by remember {
            mutableStateOf<AccountItem?>(null)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            PageTitle(
                "My Accounts",
                "Keep your account information organised"
            )

            PremiumButton(
                text = "ADD ACCOUNT",
                icon = Icons.Default.AccountBalance,
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = {
                    showAdd = true
                }
            )

            Spacer(Modifier.height(18.dp))

            repository.accounts
                .sortedByDescending { it.createdAt }
                .forEach { account ->

                    AccountCard(
                        account = account,
                        onEdit = {
                            editing = account
                        }
                    )
                }

            if (repository.accounts.isEmpty()) {
                EmptyMessage("No accounts saved yet")
            }

            Spacer(Modifier.height(20.dp))
        }

        if (showAdd) {
            AccountEditorDialog(
                title = "Add Account",
                account = null,
                onDismiss = {
                    showAdd = false
                },
                onSave = {
                    repository.addAccount(it)
                    showAdd = false
                }
            )
        }

        editing?.let { account ->

            AccountEditorDialog(
                title = "Edit Account",
                account = account,
                onDismiss = {
                    editing = null
                },
                onSave = { updated ->

                    account.name = updated.name
                    account.userId = updated.userId
                    account.password = updated.password
                    account.other1 = updated.other1
                    account.other2 = updated.other2

                    repository.saveAccounts()
                    editing = null
                }
            )
        }
    }

    @Composable
    private fun AccountCard(
        account: AccountItem,
        onEdit: () -> Unit
    ) {

        var expanded by rememberSaveable(account.id) {
            mutableStateOf(false)
        }

        val clipboard = LocalClipboard.current
        val scope = rememberCoroutineScope()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 7.dp
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded = !expanded
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = PremiumBlue
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = account.name,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )

                    IconButton(
                        onClick = onEdit
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Account",
                            tint = PremiumBlue
                        )
                    }
                }

                if (expanded) {

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(12.dp))

                    CopyableRow(
                        label = "UserID",
                        value = account.userId,
                        onCopy = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    androidx.compose.ui.platform.ClipEntry(
                                        ClipData.newPlainText(
                                            "UserID",
                                            account.userId
                                        )
                                    )
                                )
                            }
                        }
                    )

                    CopyableRow(
                        label = "Password",
                        value = account.password,
                        onCopy = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    androidx.compose.ui.platform.ClipEntry(
                                        ClipData.newPlainText(
                                            "Password",
                                            account.password
                                        )
                                    )
                                )
                            }
                        }
                    )

                    if (account.other1.isNotBlank()) {
                        DetailRow("Other-1", account.other1)
                    }

                    if (account.other2.isNotBlank()) {
                        DetailRow("Other-2", account.other2)
                    }
                }
            }
        }
    }

    @Composable
    private fun CopyableRow(
        label: String,
        value: String,
        onCopy: () -> Unit
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    color = PremiumBlueDark
                )

                Text(
                    text = value,
                    color = TextDark
                )
            }

            IconButton(
                onClick = onCopy
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "Copy"
                )
            }
        }
    }

    @Composable
    private fun DetailRow(
        label: String,
        value: String
    ) {

        Column(
            modifier = Modifier.padding(vertical = 5.dp)
        ) {

            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = PremiumBlueDark
            )

            Text(
                text = value,
                color = TextDark
            )
        }
    }

    @Composable
    private fun AccountEditorDialog(
        title: String,
        account: AccountItem?,
        onDismiss: () -> Unit,
        onSave: (AccountItem) -> Unit
    ) {

        var name by remember {
            mutableStateOf(account?.name ?: "")
        }

        var userId by remember {
            mutableStateOf(account?.userId ?: "")
        }

        var password by remember {
            mutableStateOf(account?.password ?: "")
        }

        var other1 by remember {
            mutableStateOf(account?.other1 ?: "")
        }

        var other2 by remember {
            mutableStateOf(account?.other2 ?: "")
        }

        var passwordVisible by remember {
            mutableStateOf(false)
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(title)
            },

            text = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = {
                            Text("For What Account")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userId,
                        onValueChange = {
                            userId = it
                        },
                        label = {
                            Text("UserID")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        label = {
                            Text("Password")
                        },
                        visualTransformation =
                            if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    passwordVisible = !passwordVisible
                                }
                            ) {
                                Icon(
                                    if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = other1,
                        onValueChange = {
                            other1 = it
                        },
                        label = {
                            Text("Other-1")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = other2,
                        onValueChange = {
                            other2 = it
                        },
                        label = {
                            Text("Other-2")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                AccountItem(
                                    id = account?.id
                                        ?: UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    userId = userId.trim(),
                                    password = password,
                                    other1 = other1.trim(),
                                    other2 = other2.trim(),
                                    createdAt = account?.createdAt
                                        ?: System.currentTimeMillis()
                                )
                            )
                        }
                    }
                ) {
                    Text("SAVE")
                }
            },

            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    @Composable
    private fun FilesPage(
        repository: AppRepository,
        selectedUploadUri: Uri?,
        onChooseFile: () -> Unit,
        onClearSelectedFile: () -> Unit,
        onOpenFile: (FileItem) -> Unit
    ) {

        val context = LocalContext.current

        var title by rememberSaveable {
            mutableStateOf("")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            PageTitle(
                "My Files",
                "Securely organise your important files"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Add File",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        label = {
                            Text("File Title")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    PremiumButton(
                        text = if (selectedUploadUri == null)
                            "UPLOAD FILE"
                        else
                            "FILE SELECTED",
                        icon = Icons.Default.UploadFile,
                        onClick = onChooseFile
                    )

                    selectedUploadUri?.let { uri ->

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = getFileName(context, uri),
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(10.dp))

                        Row {

                            androidx.compose.material3.TextButton(
                                onClick = onClearSelectedFile
                            ) {
                                Text("REMOVE")
                            }

                            Spacer(Modifier.width(8.dp))

                            androidx.compose.material3.TextButton(
                                onClick = {

                                    if (
                                        title.isNotBlank() &&
                                        selectedUploadUri != null
                                    ) {

                                        try {

                                            val uploadDir =
                                                File(
                                                    context.filesDir,
                                                    "uploads"
                                                )

                                            uploadDir.mkdirs()

                                            val storedName =
                                                UUID.randomUUID().toString()

                                            val destination =
                                                File(
                                                    uploadDir,
                                                    storedName
                                                )

                                            context.contentResolver
                                                .openInputStream(
                                                    selectedUploadUri
                                                )
                                                ?.use { input ->
                                                    FileOutputStream(
                                                        destination
                                                    ).use {
                                                        input.copyTo(it)
                                                    }
                                                }

                                            repository.addFile(
                                                FileItem(
                                                    title = title.trim(),
                                                    storedName = storedName,
                                                    originalName =
                                                        getFileName(
                                                            context,
                                                            selectedUploadUri
                                                        ),
                                                    mimeType =
                                                        context.contentResolver
                                                            .getType(
                                                                selectedUploadUri
                                                            ) ?: "*/*"
                                                )
                                            )

                                            title = ""
                                            onClearSelectedFile()

                                            Toast.makeText(
                                                context,
                                                "File saved successfully",
                                                Toast.LENGTH_LONG
                                            ).show()

                                        } catch (e: Exception) {

                                            Toast.makeText(
                                                context,
                                                "Unable to save file",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            ) {
                                Text("SAVE FILE")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            repository.files
                .sortedByDescending { it.createdAt }
                .forEach { file ->

                    FileCard(
                        item = file,
                        onOpen = {
                            onOpenFile(file)
                        }
                    )
                }

            if (repository.files.isEmpty()) {
                EmptyMessage("No files saved yet")
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    @Composable
    private fun FileCard(
        item: FileItem,
        onOpen: () -> Unit
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 7.dp
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = PremiumBlue,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )

                    Text(
                        text = item.originalName,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = onOpen
                ) {
                    Icon(
                        Icons.Default.FileOpen,
                        contentDescription = "Open File",
                        tint = PremiumBlue
                    )
                }
            }
        }
    }

    @Composable
    private fun BackupPage(
        onExport: () -> Unit,
        onRestore: () -> Unit
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            PageTitle(
                "Export / Restore All",
                "Create a complete backup of your Save All data"
            )

            BackupCard(
                title = "Export All Data",
                description =
                    "Export notes, accounts, saved files and PIN into one backup ZIP file.",
                icon = Icons.Default.Save,
                buttonText = "EXPORT ALL DATA",
                onClick = onExport
            )

            BackupCard(
                title = "Restore All Data",
                description =
                    "Restore your complete Save All backup using Android's official system file manager.",
                icon = Icons.Default.Restore,
                buttonText = "RESTORE ALL DATA",
                onClick = onRestore
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF2FF)
                )
            ) {

                Text(
                    text = "Android Storage Access Framework (SAF) is used. Android will open the official system file picker where available storage locations and cloud providers can be selected.",
                    modifier = Modifier.padding(18.dp),
                    color = TextDark
                )
            }
        }
    }

    @Composable
    private fun BackupCard(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        buttonText: String,
        onClick: () -> Unit
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(7.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Icon(
                    icon,
                    contentDescription = null,
                    tint = PremiumBlue,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextDark
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    text = description,
                    color = Color.DarkGray
                )

                Spacer(Modifier.height(16.dp))

                PremiumButton(
                    text = buttonText,
                    icon = icon,
                    onClick = onClick
                )
            }
        }
    }

    @Composable
    private fun ControlPanelPage(repository: AppRepository) {

        var deleteNotes by rememberSaveable {
            mutableStateOf(false)
        }

        var deleteAccounts by rememberSaveable {
            mutableStateOf(false)
        }

        var deleteFiles by rememberSaveable {
            mutableStateOf(false)
        }

        var resetPin by rememberSaveable {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            PageTitle(
                "Control Panel",
                "Manage and permanently delete your saved data"
            )

            ControlCard(
                title = "Delete Notes",
                description =
                    "Select notes individually and permanently delete them.",
                icon = Icons.Default.Delete,
                onClick = {
                    deleteNotes = true
                }
            )

            ControlCard(
                title = "Delete Accounts",
                description =
                    "Select saved accounts individually and permanently delete them.",
                icon = Icons.Default.Delete,
                onClick = {
                    deleteAccounts = true
                }
            )

            ControlCard(
                title = "Delete Files",
                description =
                    "Select saved files individually and permanently delete them.",
                icon = Icons.Default.Delete,
                onClick = {
                    deleteFiles = true
                }
            )

            ControlCard(
                title = "Reset Your App PIN",
                description =
                    "Set a new 6 digit PIN for opening Save All.",
                icon = Icons.Default.Refresh,
                onClick = {
                    resetPin = true
                }
            )

            Spacer(Modifier.height(20.dp))
        }

        if (deleteNotes) {
            MultiDeleteDialog(
                title = "Delete Notes",
                items = repository.notes
                    .sortedByDescending { it.createdAt },
                getId = { it.id },
                getName = { it.title },
                onDismiss = {
                    deleteNotes = false
                },
                onDelete = { ids ->
                    repository.deleteNotes(ids)
                    deleteNotes = false
                }
            )
        }

        if (deleteAccounts) {
            MultiDeleteDialog(
                title = "Delete Accounts",
                items = repository.accounts
                    .sortedByDescending { it.createdAt },
                getId = { it.id },
                getName = { it.name },
                onDismiss = {
                    deleteAccounts = false
                },
                onDelete = { ids ->
                    repository.deleteAccounts(ids)
                    deleteAccounts = false
                }
            )
        }

        if (deleteFiles) {
            MultiDeleteDialog(
                title = "Delete Files",
                items = repository.files
                    .sortedByDescending { it.createdAt },
                getId = { it.id },
                getName = { it.title },
                onDismiss = {
                    deleteFiles = false
                },
                onDelete = { ids ->
                    repository.deleteFiles(ids)
                    deleteFiles = false
                }
            )
        }

        if (resetPin) {
            ResetPinDialog(
                repository = repository,
                onDismiss = {
                    resetPin = false
                }
            )
        }
    }

    @Composable
    private fun ControlCard(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        onClick: () -> Unit
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 7.dp
                )
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {

            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint =
                        if (title.contains("Delete"))
                            DangerRed
                        else
                            PremiumBlue,
                    modifier = Modifier.size(38.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column {

                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = TextDark
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }

    @Composable
    private fun <T> MultiDeleteDialog(
        title: String,
        items: List<T>,
        getId: (T) -> String,
        getName: (T) -> String,
        onDismiss: () -> Unit,
        onDelete: (Set<String>) -> Unit
    ) {

        val selected = remember {
            mutableStateListOf<String>()
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(title)
            },

            text = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    if (items.isEmpty()) {
                        Text("Nothing available to delete.")
                    }

                    items.forEach { item ->

                        val id = getId(item)
                        val checked = id in selected

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (checked) {
                                        selected.remove(id)
                                    } else {
                                        selected.add(id)
                                    }
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            androidx.compose.material3.Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    if (it) {
                                        selected.add(id)
                                    } else {
                                        selected.remove(id)
                                    }
                                }
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = getName(item),
                                color = TextDark
                            )
                        }
                    }
                }
            },

            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (selected.isNotEmpty()) {
                            onDelete(selected.toSet())
                        }
                    }
                ) {
                    Text(
                        text = "DELETE (${selected.size})",
                        color = DangerRed
                    )
                }
            },

            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    @Composable
    private fun ResetPinDialog(
        repository: AppRepository,
        onDismiss: () -> Unit
    ) {

        var pin by rememberSaveable {
            mutableStateOf("")
        }

        var confirm by rememberSaveable {
            mutableStateOf("")
        }

        var error by rememberSaveable {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("Reset Your App PIN")
            },

            text = {

                Column {

                    Text(
                        "Enter a new 6 digit PIN."
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (
                                it.length <= 6 &&
                                it.all(Char::isDigit)
                            ) {
                                pin = it
                                error = ""
                            }
                        },
                        label = {
                            Text("New 6 Digit PIN")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation =
                            PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirm,
                        onValueChange = {
                            if (
                                it.length <= 6 &&
                                it.all(Char::isDigit)
                            ) {
                                confirm = it
                                error = ""
                            }
                        },
                        label = {
                            Text("Confirm PIN")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation =
                            PasswordVisualTransformation()
                    )

                    if (error.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = error,
                            color = DangerRed
                        )
                    }
                }
            },

            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {

                        when {
                            pin.length != 6 -> {
                                error = "PIN must contain exactly 6 digits."
                            }

                            pin != confirm -> {
                                error = "Both PINs do not match."
                            }

                            else -> {
                                repository.setPin(pin)
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("RESET PIN")
                }
            },

            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    @Composable
    private fun PremiumButton(
        text: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {

        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PremiumBlueLight,
                            PremiumBlue,
                            PremiumBlueDark
                        )
                    )
                ),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {

            Icon(
                icon,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = text,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    private fun EmptyMessage(message: String) {

        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign =
                androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    private fun indianToday(): String {
        return LocalDate
            .now(ZoneId.of("Asia/Kolkata"))
            .format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
    }

    private fun getFileName(
        context: Context,
        uri: Uri
    ): String {

        var name = "Selected File"

        context.contentResolver
            .query(
                uri,
                null,
                null,
                null,
                null
            )
            ?.use { cursor ->

                val index =
                    cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                if (
                    index >= 0 &&
                    cursor.moveToFirst()
                ) {
                    name = cursor.getString(index)
                }
            }

        return name
    }
}
