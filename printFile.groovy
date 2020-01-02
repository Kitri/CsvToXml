import csv.CsvParser
import csv.CsvSection
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.event.*;
import javax.print.attribute.standard.MediaSizeName;

/*
*  To run this script:
*  Change the paths inside the lines below. Note that if the output folder is inside of the input folder it
*  needs to exist before running the script, else the script will pick it up as a "new file" and fail
*
*  From commandline, navigate to the directory containing printFile.groovy. Make sure the csv folder is in the same directory
*  Run: 
*     groovy printFile.groovy
*
*  To exit the script, escape with ctrl^C
*
*/

//================== Change these paths =====================
// Path watchPath = Paths.get("/Users/merciamalan/My Files/atletiek/watch/")
// String outputPath = "/Users/merciamalan/My Files/atletiek/output/"
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
        System.out.println("Found file, sleeping")
        Thread.sleep(1000)
        String filename = event.context().getFileName().toString()
        String uri = watchPath.toString() + "/" + filename

        boolean isTextFile = filename.endsWith(".txt")

        String newFile = copyFile(filename, uri, outputPath)

        //------------ Hierdie print kan dalk spammy raak, jy kan hom uit comment
        System.out.println("Copied file to " + newFile)

        if(isTextFile) {
            System.out.println("Printing file: " + filename)
            printFile(uri)
        }
    }

    //reset is invoked to put the key back to ready state
    boolean valid = key.reset()
    //If the key is invalid, just exit.
    if ( !valid ) {
        break
    }
}

String copyFile(String filename, String sourceUri, String destUri) {
    File source = new File(sourceUri)
    File dest = new File(destUri  + filename)

    Path output = Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING); 

    return output.toString()
}

void printFile(String uri) {
    DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
    PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
    aset.add(MediaSizeName.ISO_A4);
    PrintService service = PrintServiceLookup.lookupDefaultPrintService(); 

    if (service != null) {
        String printServiceName = service.getName();

        // ---------------Uncomment hierdie lyn om te sien na watse printer hy print ------------
        // System.out.println("Printing to default printer: " + printServiceName);

        try {
            DocPrintJob job = service.createPrintJob();
            FileInputStream fis = new FileInputStream(uri);
            Doc doc = new SimpleDoc(fis, flavor, null);
            job.print(doc, aset);
            } catch (FileNotFoundException fe) {
                System.out.println("File not found while printing " + fe)
            } catch (PrintException e) {
                System.out.println("Print exception " + e)
            }

        //------------ moet miskien die doen, nie seker nie ---------------
        ejectPage(flavor, service)

    } else {
        System.out.println("No default print service found");
    }

}

// send FF to eject the page
void ejectPage(DocFlavor flavor, PrintService service ) {
    InputStream ff = new ByteArrayInputStream("\f".getBytes());
    Doc docff = new SimpleDoc(ff, flavor, null);
    DocPrintJob jobff = service.createPrintJob();
    jobff.print(docff, null);
}


