package it.recupera.mp33;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Downloads started by the Facebook app end up in Download/.!%#@$ (temp folder)
 * and then in the Samsung My Files trash. Copy .mp33/.mp/.mid back to Download,
 * renaming .mp33/.mp to .mp3, and remove them from the trash.
 */
public class MainActivity extends Activity {
    static final File ROOT = Environment.getExternalStorageDirectory();
    static final File TRASH = new File(ROOT, "Android/.Trash/com.sec.android.app.myfiles");
    static final File TEMP = new File(ROOT, "Download/.!%#@$");
    static final File DOWNLOAD = new File(ROOT, "Download");

    boolean askedPermission;

    @Override
    protected void onResume() {
        super.onResume();
        if (!Environment.isExternalStorageManager()) {
            if (askedPermission) {
                toast("Serve il permesso \"Accesso a tutti i file\"");
                finish();
                return;
            }
            askedPermission = true;
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            return;
        }
        toast(recover());
        finish();
    }

    String recover() {
        List<String> copied = new ArrayList<>();
        int errors = 0;

        for (File f : find(TRASH)) {
            try {
                copy(f, copied);
                deleteWithEmptyParents(f);
            } catch (IOException e) {
                errors++;
            }
        }
        // The temp folder may hold a download still in progress: copy only, never delete.
        long now = System.currentTimeMillis();
        for (File f : find(TEMP)) {
            if (now - f.lastModified() < 30_000) continue;
            try {
                copy(f, copied);
            } catch (IOException e) {
                errors++;
            }
        }

        if (!copied.isEmpty()) {
            MediaScannerConnection.scanFile(getApplicationContext(),
                    copied.toArray(new String[0]), null, null);
        }
        String msg = copied.isEmpty() ? "Nessun file nuovo da recuperare"
                : "Recuperati " + copied.size() + " file in Download";
        return errors == 0 ? msg : msg + " (" + errors + " errori)";
    }

    /** .mp33/.mp/.mid files under dir, shortest name first so "X.mp33" wins over "X-1.mp33". */
    static List<File> find(File dir) {
        List<File> out = new ArrayList<>();
        walk(dir, out);
        Collections.sort(out, new Comparator<File>() {
            public int compare(File a, File b) {
                return a.getName().length() - b.getName().length();
            }
        });
        return out;
    }

    static void walk(File dir, List<File> out) {
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) walk(f, out);
            else if (wanted(f.getName())) out.add(f);
        }
    }

    static boolean wanted(String name) {
        String n = name.toLowerCase();
        return n.endsWith(".mp33") || n.endsWith(".mp") || n.endsWith(".mid");
    }

    /** Copies src into Download; skips it if the same file (same name or "-N" twin, same size) is already there. */
    static void copy(File src, List<String> copied) throws IOException {
        String name = src.getName().replaceFirst("(?i)\\.(mp33|mp)$", ".mp3");

        File dst = new File(DOWNLOAD, name);
        File twin = new File(DOWNLOAD, name.replaceFirst("-\\d+(\\.\\w+)$", "$1"));
        if (sameSize(dst, src) || sameSize(twin, src)) return;

        int dot = name.lastIndexOf('.');
        for (int i = 2; dst.exists(); i++) {
            dst = new File(DOWNLOAD, name.substring(0, dot) + " (" + i + ")" + name.substring(dot));
        }
        Files.copy(src.toPath(), dst.toPath());
        copied.add(dst.getPath());
    }

    static boolean sameSize(File a, File b) {
        return a.isFile() && a.length() == b.length();
    }

    static void deleteWithEmptyParents(File f) {
        f.delete();
        File p = f.getParentFile();
        while (p != null && !p.equals(TRASH) && p.delete()) p = p.getParentFile();
    }

    void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}
