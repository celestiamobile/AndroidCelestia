package space.celestia.ziputils

public class ZipUtils {
    companion object {
        @JvmStatic
        public external fun unzip(sourcePath: String, destinationFolderPath: String): Boolean
    }
}