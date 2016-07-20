package src.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import src.Files.AnalyzeDatResults;
import src.Files.ConvertDat;
import src.Files.DatFile;
import src.Files.NotDatFile;

public class CLIDatCon {
	private static String version = "2.3.1";

	public static void main(String[] args) {
		String datFileName = "";
		String csvFileName = "";
		if (args.length == 0) {
			System.out.println(version);
			System.exit(0);
		}
		if (args.length != 2) {
			System.out.println("BadArgs");
			System.exit(0);
		}
		datFileName = args[0];
		csvFileName = args[1];

		File csvFile = new File(csvFileName);
		DatFile datFile;
		long timeOffset = 0;
		try {
			datFile = new DatFile(datFileName);
			ConvertDat convertDat = new ConvertDat(datFile);
			datFile.findMarkers();
			if (datFile.motorStartTick != 0) {
				timeOffset = datFile.motorStartTick;
			}
			if (datFile.flightStartTick != -1) {
				timeOffset = datFile.flightStartTick;
			}
			convertDat.timeOffset = timeOffset;
			datFile.reset();
			convertDat.createRecords();
			convertDat.sampleRate = 30;
			PrintStream csvPS = new PrintStream(csvFile);
			convertDat.csvPS = csvPS;
			AnalyzeDatResults results = convertDat.analyze(true);
			csvPS.close();
			System.out.println("OK");
		} catch (IOException | NotDatFile e) {
			e.printStackTrace();
		} catch (FileEnd e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
