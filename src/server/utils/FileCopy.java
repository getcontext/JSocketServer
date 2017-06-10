package server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.channels.FileChannel;


/**
 * klasa odpowiada za operacje kopiowania i przenoszenia plik�w
 *
 * @author andrzej.salamon@biztech.pl
 *
 */
public class FileCopy {
    /**
     * kopiuje wskazany plik do wskazanego miejsca docelowego
     *
     * @param fromFileName
     *            oryginalna, pe�na �cie�ka do pliku, kt�rego chcemy skopiowa�,
     *            wraz z jego pe�n� nazw�
     * @param toFileName
     *            pe�na �cie�ka do miejsca, w kt�re plik ma by� skopiowany wraz
     *            z jego now� nazw�
     * @throws IOException
     */

    // byte[] buffer;

    public static void copy(String fromFileName, String toFileName) throws IOException {
        final File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists())
            throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
        if (!fromFile.isFile())
            throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
        if (!fromFile.canRead())
            throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

        if (toFile.isDirectory())
            toFile = new File(toFile, fromFile.getName());

        if (toFile.exists()) {
            if (!toFile.canWrite())
                throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
            // System.out.print("Overwrite existing file " + toFile.getName()
            // + "? (Y/N): ");
            // System.out.flush();
            // BufferedReader in = new BufferedReader(new InputStreamReader(
            // System.in));
            // String response = in.readLine();
            // if (!response.equals("Y") && !response.equals("y"))
            // throw new IOException("FileCopy: "
            // + "existing file was not overwritten.");
        } else {
            String parent = toFile.getParent();
            if (parent == null)
                parent = System.getProperty("user.dir");
            final File dir = new File(parent);
            if (!dir.exists())
                throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
            if (dir.isFile())
                throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
            if (!dir.canWrite())
                throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
        }


        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(fromFile).getChannel();
            destination = new FileOutputStream(toFile).getChannel();
            destination.transferFrom(source, 0, source.size());

        } finally {
            if (source != null) {
                source.close();
                source = null;
            }
            if (destination != null) {
                destination.close();
                destination = null;
            }
            // try {
            //   Thread.sleep(500);
            // } catch (InterruptedException e) {
            // }
        }
    }
}
