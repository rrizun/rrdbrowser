package rrdbrowser;

import java.io.*;
import java.util.*;

import com.google.common.base.*;
import com.google.common.io.*;

/**
 * Process ByteSource.
 */
public class Exec {
  /**
   * Returns a new {@link ByteSource} for reading stdout bytes from the given
   * process.
   * 
   * @param command
   * @return
   */
  public static ByteSource asByteSource(final List<String> command) {
    return new ByteSource() {
      ByteArrayOutputStream stderr = new ByteArrayOutputStream();
      @Override
      public InputStream openStream() throws IOException {
        out.println(Strings.repeat("-", 70));
        out.println(command);
        out.println(Strings.repeat("-", 70));
        ProcessBuilder builder = new ProcessBuilder(command);
        final Process p = builder.start();
        final Thread t = new Thread() {
          @Override
          public void run() {
            try {
              ByteStreams.copy(p.getErrorStream(), stderr);
            } catch (IOException ioe) {
              ioe.printStackTrace();
            }
          }
        };
        t.start();
        return new FilterInputStream(p.getInputStream()) {
          public void close() throws IOException {
            try {
              t.join();
              if (p.waitFor() != 0)
                throw new IOException("" + p.exitValue() + command + new String(stderr.toByteArray()).trim());
            } catch (InterruptedException e) {
              throw new IOException(e);
            } finally {
              p.destroy();
            }
          }
        };
      }
    };
  }
  static PrintStream out = System.out;
}
