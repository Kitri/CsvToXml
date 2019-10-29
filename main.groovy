import csv.CsvParser
import csv.CsvSection
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;

/*
*  To run this script:
*  Change the paths inside the lines below. Note that if the output folder is inside of the input folder it
*  needs to exist before running the script, else the script will pick it up as a "new file" and fail
*
*  From commandline, navigate to the directory containing main.groovy. Make sure the csv folder is in the same directory
*  Run: 
*     groovy main.groovy
*
*  To exit the script, escape with ctrl^C
*
*/

//================== Change these paths =====================
// Path watchPath = Paths.get("C:/Files/Results_0008_100m_Men25_A2.csv")
Path watchPath = Paths.get("C:/Users/Jacques/Versus/Publish/BroadCast/")
String outputPath = "C:/Users/Jacques/Versus/Publish/BroadCast/"
//============================================================

WatchService watchService = FileSystems.getDefault().newWatchService()

watchPath.register(
        watchService,
        StandardWatchEventKinds.ENTRY_CREATE);

System.out.println("Waiting for files in folder " + watchPath.toString())

for ( ; ; ) {
    WatchKey key = watchService.take()

    //Poll all the events queued for the key
    for ( WatchEvent<?> event: key.pollEvents()){
	    //If files gets written to the folder from another application it is possible that this one will pick it up 
        // before it's done writing, resulting in half a read. In that case, cheat it with the below thread.sleep
        Thread.sleep(1000)
        String filename = event.context().getFileName().toString()
        String uri = watchPath.toString() + "/" + filename
        def xml = convertFile(uri)
        writeXmlToFile(outputPath, filename, xml)
    }

    //reset is invoked to put the key back to ready state
    boolean valid = key.reset()
    //If the key is invalid, just exit.
    if ( !valid ) {
        break
    }
}


def String convertFile(String uri) {
    System.out.println("Converting: " + uri)

    CsvSection eventSection = new CsvSection([name: "Description", length: 1])
    CsvSection detailSection = new CsvSection([name: "Details", length: 0])

    CsvParser parser = new CsvParser(uri, eventSection, detailSection)
    return parser.convertToXml()
}

def writeXmlToFile(String outputPath, String filename, String xml) {
    File directory = new File(outputPath)
    if(!directory.exists()) { directory.mkdir()}

    String outputFileName = filename.startsWith("Starter") ? "Starter" : "Result"
    File file = new File(outputPath  + outputFileName + ".xml")
    file.write(xml)

    System.out.println("Done writing to " + file.toString())
}
