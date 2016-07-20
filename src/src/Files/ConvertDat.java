/* ConvertDat class

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that redistribution of source code include
the following disclaimer in the documentation and/or other materials provided
with the distribution.

THIS SOFTWARE IS PROVIDED BY ITS CREATOR "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE CREATOR OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package src.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import src.DatConRecs.*;

public class ConvertDat {

	public final static boolean EXPERIMENTAL = false;
	static public final String version = "2.3.1";

	public DatFile _datFile = null;

	private FileEnd _fileEnd = new FileEnd();

	public ConvertDat(DatFile datFile) {
		_datFile = datFile;
	}

	public ConvertDat() {
	}

	long tickNo = 0;

	public long tickRangeLower = 0;

	public long tickRangeUpper = Long.MAX_VALUE;

	public float sampleRate = (float) 600.0;

	public long timeOffset = 0;

	public Record records[] = null;

	public int kmlType = -1; // -1 = none, 0 = groundTrack, 1 = profile

	public File kmlFile;

	public String kmlFileName;

	public float homePointElevation = 0.0f;

	public boolean csvEventLogOutput = false;

	public PrintStream eloPS = null;

	public PrintStream cloPS = null;

	public PrintStream kmlPS = null;

	public PrintStream csvPS = null;

	public PrintStream tloPS = null;
	private boolean printVersion;

	public AnalyzeDatResults analyze(boolean printVersion) throws IOException {
		this.printVersion = printVersion;
		int sampleSize = (int) (600.0 / sampleRate);
		try {
			_datFile.setStartOfRecord(128);
			long fileLength = _datFile.getLength();
			// If there is a .csv being produced go ahead and output
			// the first row containing the column headings
			if (csvPS != null) {
				csvPS.print("Tick#,offsetTime");
				printCsvLine(csvPS, lineType.HEADER);
			}
			long lastTickNoPrinted = -sampleSize;
			// Main loop that gets a tick#Group and processes all the records in
			// that group
			while (true) {
				if (_datFile.getPos() > fileLength - 8) {
					throw (_fileEnd);
				}
				// Get the next tick#Group
				DatFile.tickGroup tG = _datFile.getTickGroup();
				boolean processedSomePayloads = false;
				tickNo = tG.tickNo;
				if (tickNo <= tickRangeUpper) {
					for (int tgIndex = 0; tgIndex < tG.numElements; tgIndex++) {
						int payloadType = tG.payloadType[tgIndex];
						long payloadStart = tG.start[tgIndex];
						int payloadLength = tG.length[tgIndex];
						short subType = tG.subType[tgIndex];
						for (int i = 0; i < records.length; i++) {
							// For each record found in this tick#Group is it
							// something
							// that we want to process
							if (records[i].isType(payloadType, subType)) {
								Payload payload = new Payload(_datFile,
										payloadStart, payloadLength,
										payloadType, subType, tickNo);
								((Record) records[i]).process(payload);
								processedSomePayloads = true;
							}
						}
					}
					if (tickRangeLower <= tickNo) {
						// if some payloads in this tick#Group were processed
						// then output the .csv line
						if ((csvPS != null) && processedSomePayloads
								&& tickNo >= lastTickNoPrinted + sampleSize) {
							csvPS.print(tickNo + ","
									+ Util.timeString(tickNo, timeOffset));
							printCsvLine(csvPS, lineType.LINE);
							lastTickNoPrinted = tickNo;
						}
					}
				}
			}
		} catch (FileEnd ex) {
		} catch (Corrupted ex) {
		}
		return _datFile.getResults();
	}

	private enum lineType {
		HEADER, LINE
	};

	private void printCsvValue(String header, float value, lineType lineT,
			boolean valid) {
		if (lineT == lineType.HEADER) {
			csvPS.print("," + header);
		} else {
			csvPS.print(",");
			if (valid)
				csvPS.print(value);
		}
	}

	private void printCsvValue(String header, double value, lineType lineT,
			boolean valid) {
		if (lineT == lineType.HEADER) {
			csvPS.print("," + header);
		} else {
			csvPS.print(",");
			if (valid)
				csvPS.print(value);
		}
	}

	private void printCsvValue(String header, int value, lineType lineT,
			boolean valid) {
		if (lineT == lineType.HEADER) {
			csvPS.print("," + header);
		} else {
			csvPS.print(",");
			if (valid)
				csvPS.print(value);
		}
	}

	private void printCsvValue(String header, String value, lineType lineT,
			boolean valid) {
		if (lineT == lineType.HEADER) {
			csvPS.print("," + header);
		} else {
			csvPS.print(",");
			if (valid)
				csvPS.print(value);
		}
	}

	double lastLatRad = 0.0;
	double lastLongRad = 0.0;
	long lastTickNo = 0;
	private boolean notFirstLine = false;

	private void printCsvLine(PrintStream _csv, lineType lineT) {
		if (lineT == lineType.HEADER)
			notFirstLine = false;
		// Rec207
		float vel = (float) Math.sqrt(Record207.current.velN
				* Record207.current.velN + Record207.current.velE
				* Record207.current.velE + Record207.current.velD
				* Record207.current.velD);
		float velH = (float) Math.sqrt(Record207.current.velN
				* Record207.current.velN + Record207.current.velE
				* Record207.current.velE);
		float magMod = (int) Math.sqrt(Record207.current.magX
				* Record207.current.magX + Record207.current.magY
				* Record207.current.magY + Record207.current.magZ
				* Record207.current.magZ);
		float error = (float) Math.sqrt(Record207.current.diffX
				* Record207.current.diffX + Record207.current.diffY
				* Record207.current.diffY + Record207.current.diffZ
				* Record207.current.diffZ);
		float accel = (float) Math.sqrt(Record207.current.accelX
				* Record207.current.accelX + Record207.current.accelY
				* Record207.current.accelY + Record207.current.accelZ
				* Record207.current.accelZ);
		float gyro = (float) Math.sqrt(Record207.current.gyroX
				* Record207.current.gyroX + Record207.current.gyroY
				* Record207.current.gyroY + Record207.current.gyroZ
				* Record207.current.gyroZ);
		Quaternion q = new Quaternion(Record207.current.quatW,
				Record207.current.quatX, Record207.current.quatY,
				Record207.current.quatZ);
		double[] eulerAngs = q.toEuler();
		double pitch = Math.toDegrees(eulerAngs[0]);
		double roll = Math.toDegrees(eulerAngs[1]);
		double yaw = Math.toDegrees(eulerAngs[2]);
		printCsvValue("flightTime(msec)", Record42_12.current.flightTime,
				lineT, true);
		double longitude = Math.toDegrees(Record207.current.longRad);
		double latitude = Math.toDegrees(Record207.current.latRad);
		boolean coordsValid = false;
		if (longitude != 0.0 && latitude != 0.0 && Math.abs(longitude) > 0.0175
				&& Math.abs(latitude) > 0.0175) {
			coordsValid = true;
		}
		printCsvValue("Longitude", longitude, lineT, coordsValid);
		printCsvValue("Latitude", latitude, lineT, coordsValid);
		printCsvValue("numSats", Record207.current.numSats, lineT, true);
		printCsvValue("gpsHealth", Record152_0.current.gpsHealth, lineT, true);
		printCsvValue("gpsAltitude(meters)", Record207.current.gpsAlt, lineT,
				true);
		printCsvValue("baroAlt(meters)", Record207.current.baroAlt, lineT, true);
		printCsvValue("vpsHeight(M)", Record92_3.current.vpsHeight, lineT,
				(Record92_3.current.vpsQuality > 190));
		printCsvValue("relativeHeight", Record42_12.current.height, lineT, true);
		printCsvValue("accelX(M/S2)", Record207.current.accelX, lineT, true);
		printCsvValue("accelY(M/S2)", Record207.current.accelY, lineT, true);
		printCsvValue("accelZ(M/S2)", Record207.current.accelZ, lineT, true);
		printCsvValue("accel(M/S2)", accel, lineT, true);
		printCsvValue("gyroX(degrees/s)", Record207.current.gyroX, lineT, true);
		printCsvValue("gyroY(degrees/s)", Record207.current.gyroY, lineT, true);
		printCsvValue("gyroZ(degrees/s)", Record207.current.gyroZ, lineT, true);
		printCsvValue("gyro(degrees/s)", gyro, lineT, true);

		printCsvValue("errorX", Record207.current.diffX, lineT, true);
		printCsvValue("errorY", Record207.current.diffY, lineT, true);
		printCsvValue("errorZ", Record207.current.diffZ, lineT, true);
		printCsvValue("error", error, lineT, true);

		printCsvValue("magX", Record207.current.magX, lineT, true);
		printCsvValue("magY", Record207.current.magY, lineT, true);
		printCsvValue("magZ", Record207.current.magZ, lineT, true);
		printCsvValue("magMod", magMod, lineT, true);
		printCsvValue("velN(M/S)", Record207.current.velN, lineT, true);
		printCsvValue("velE(M/S)", Record207.current.velE, lineT, true);
		printCsvValue("velD(M/S)", Record207.current.velD, lineT, true);
		printCsvValue("vel(M/S)", vel, lineT, true);
		printCsvValue("velH(M/S)", velH, lineT, true);
		double velGPS = 0.0;
		if (lineT == lineType.HEADER) {
			printCsvValue("velGPS-velH(M/S)", 0.0, lineT, true);
		} else {
			if (notFirstLine) {
				double distance = Util.distance(Record207.current.latRad,
						Record207.current.longRad, lastLatRad, lastLongRad);
				velGPS = distance / (((double) (tickNo - lastTickNo)) / 600.0);
			} else {
				velGPS = 0.0;
			}
			lastLatRad = Record207.current.latRad;
			lastLongRad = Record207.current.longRad;
			printCsvValue("velGPS-velH(M/S)", velGPS - velH, lineT, true);
		}

		printCsvValue("quatW", Record207.current.quatW, lineT, true);
		printCsvValue("quatX", Record207.current.quatX, lineT, true);
		printCsvValue("quatY", Record207.current.quatY, lineT, true);
		printCsvValue("quatZ", Record207.current.quatZ, lineT, true);

		printCsvValue("Roll", roll, lineT, true);
		printCsvValue("Pitch", pitch, lineT, true);
		printCsvValue("Yaw", yaw, lineT, true);
		printCsvValue("Yaw360", ((yaw + 360.0) % 360.0), lineT, true);

		printCsvValue("totalGyroZ", Record207.current.totalZGyro, lineT, true);
		if (lineT == lineType.HEADER) {
			printCsvValue("magYaw", 0.0, lineT, true);
		} else {
			Quaternion qAcc = Quaternion.fromAngles(eulerAngs[0], eulerAngs[1],
					0.0);
			double magX = Record207.current.magX;
			double magY = Record207.current.magY;
			double magZ = Record207.current.magZ;
			Quaternion x = new Quaternion(0.0, magX, magY, magZ);
			Quaternion magXYPlane = qAcc.times(x).times(qAcc.conjugate());
			double X = magXYPlane.getX();
			double Y = magXYPlane.getY();
			double magYaw = Math.toDegrees(-Math.atan2(Y, X));
			printCsvValue("magYawX", magYaw, lineT, true);
					}

		double lbrfDiff = Record218_241.current.lBackSpeed
				- Record218_241.current.rFrontSpeed;
		double rblfDiff = Record218_241.current.rBackSpeed
				- Record218_241.current.lFrontSpeed;

		double thrust1 = Math.toDegrees(Math.atan2(lbrfDiff, rblfDiff));
		double thrust2 = (thrust1 + 315.0) % 360.0;
		double thrustTheta = thrust2;
		if (thrust2 > 180.0) {
			thrustTheta = thrust2 - 360.0;
		}
		printCsvValue("thrustAngle", thrustTheta, lineT, true);

		double distanceHP = Util.distance(Record207.current.latRad,
				Record207.current.longRad, Record255_1.current.latitudeHP,
				Record255_1.current.longitudeHP);
		printCsvValue("homePointLongitude",
				Record255_1.current.longitudeHPDegrees, lineT,
				(Record255_1.current.validHP));
		printCsvValue("homePointLatitude",
				Record255_1.current.latitudeHPDegrees, lineT,
				(Record255_1.current.validHP));
		printCsvValue("homePointAltitude", Record255_1.current.heightHP, lineT,
				(Record255_1.current.validHP));
		printCsvValue("geoMagDeclination", Record255_1.current.declination,
				lineT, (Record255_1.current.validHP));
		printCsvValue("geoMagInclination", Record255_1.current.inclination,
				lineT, (Record255_1.current.validHP));
		printCsvValue("distancHP(M)", distanceHP, lineT,
				(Record255_1.current.validHP));

		printCsvValue("distanceTravelled(M)",
				Record207.current.distanceTraveled, lineT, true);
		printCsvValue("directionOfTravel", Record207.current.bearingDeclined,
				lineT, true);
		printCsvValue("directionOfTravelTrue", Record207.current.bearingTrue,
				lineT, true);
		printCsvValue("IMUTemp(C)", Record207.current.imuTemp, lineT, true);

		// Rec42_12
		Record42_12.current.setStateStrings();
		printCsvValue("flyCState", Record42_12.current.flyc_state, lineT, true);
		printCsvValue("flyCState:String", Record42_12.current.FLCS, lineT, true);
		printCsvValue("nonGPSCause", Record42_12.current.nonGPSError, lineT,
				true);
		printCsvValue("nonGPSCause:String", Record42_12.current.NGPE, lineT,
				true);
		printCsvValue("DW flyCState", Record42_12.current.dwflyState, lineT,
				true);

		int connectedToRC = 0;
		if (Record42_12.current.connectedToRC == 0) {
			connectedToRC = 1;
		}
		printCsvValue("conectedToRC", connectedToRC, lineT, true);

		if (ConvertDat.EXPERIMENTAL) {
			printCsvValue("X:Roll", Record42_12.current.roll, lineT, true);
			printCsvValue("X:Pitch", Record42_12.current.pitch, lineT, true);
			printCsvValue("X:Yaw", Record42_12.current.yaw, lineT, true);
			printCsvValue("motorStartFailure",
					String.format("%02X", Record42_12.current.failure), lineT,
					true);

		}
		// Rec68_17 Battery Stuff
		printCsvValue("Current", Record68_17.current.crrnt, lineT, true);
		printCsvValue("Volt1", Record68_17.current.volt1, lineT, true);
		printCsvValue("Volt2", Record68_17.current.volt2, lineT, true);
		printCsvValue("Volt3", Record68_17.current.volt3, lineT, true);
		printCsvValue("Volt4", Record68_17.current.volt4, lineT, true);

		printCsvValue("Volt5", Record68_17.current.volt5, lineT,
				(Record255_1.current.inspire1));
		printCsvValue("Volt6", Record68_17.current.volt6, lineT,
				(Record255_1.current.inspire1));

		printCsvValue("totalVolts", Record68_17.current.totalVolts, lineT, true);
		printCsvValue("voltSpread", Record68_17.current.voltDiff, lineT, true);
		printCsvValue("Watts", Record68_17.current.watts, lineT, true);
		printCsvValue("batteryTemp(C)", Record68_17.current.temp, lineT, true);
		printCsvValue("ratedCapacity", Record68_17.current.ratedCapacity,
				lineT, true);
		printCsvValue("remaingCapacity", Record68_17.current.remainingCapacity,
				lineT, true);
		printCsvValue("percentageCapacity",
				Record68_17.current.relativeCapacity, lineT, true);

		printCsvValue("usefulTime", Record30_18.current.batteryUsefulTime,
				lineT, (Record30_18.current.validBUT));

		printCsvValue("percentageVolts", Record30_18.current.voltagePercent,
				lineT, (Record30_18.current.validVP));

		printCsvValue("batteryCycleCount",
				Record255_2.current.batteryCycleCount, lineT, true);
		printCsvValue("batteryLifePercentage",
				Record255_2.current.batteryPercentage, lineT, true);
		printCsvValue("batteryBarCode", Record255_1.current.batteryBarCode,
				lineT, true);

		printCsvValue("minCurrent", Record68_17.current.minCurrent, lineT, true);
		printCsvValue("maxCurrent", Record68_17.current.maxCurrent, lineT, true);
		printCsvValue("avgCurrent", Record68_17.current.avgCurrent, lineT, true);

		printCsvValue("minVolts", Record68_17.current.minVolts, lineT, true);
		printCsvValue("maxVolts", Record68_17.current.maxVolts, lineT, true);
		printCsvValue("avgVolts", Record68_17.current.avgVolts, lineT, true);

		printCsvValue("minWatts", Record68_17.current.minWatts, lineT, true);
		printCsvValue("maxWatts", Record68_17.current.maxWatts, lineT, true);
		printCsvValue("avgWatts", Record68_17.current.avgWatts, lineT, true);

		// Rec44_52
		printCsvValue("Gimbal:roll", Math.toDegrees(Record44_52.current.roll),
				lineT, true);
		printCsvValue("Gimbal:pitch",
				Math.toDegrees(Record44_52.current.pitch), lineT, true);
		printCsvValue("Gimbal:yaw", Math.toDegrees(Record44_52.current.yaw),
				lineT, true);

		Quaternion qGimbal = new Quaternion(Record44_52.current.quatW,
				Record44_52.current.quatX, Record44_52.current.quatY,
				Record44_52.current.quatZ);
		RollPitchYaw rpy = qGimbal.toRollPitchYaw();
		printCsvValue("Gimbal:Xroll", rpy.getRollDeg(), lineT, true);
		printCsvValue("Gimbal:Xpitch", rpy.getPitchDeg(), lineT, true);
		printCsvValue("Gimbal:Xyaw", rpy.getYawDeg(), lineT, true);

		printCsvValue("MotorCmnd:RFront", Record44_52.current.rFront, lineT,
				true);
		printCsvValue("MotorCmnd:LFront", Record44_52.current.lFront, lineT,
				true);
		printCsvValue("MotorCmnd:LBack", Record44_52.current.lBack, lineT, true);
		printCsvValue("MotorCmnd:RBack", Record44_52.current.rBack, lineT, true);

		// Rec218_241
		printCsvValue("MotorSpeed:RFront", Record218_241.current.rFrontSpeed,
				lineT, !(Record255_1.current.standard));
		printCsvValue("MotorSpeed:LFront", Record218_241.current.lFrontSpeed,
				lineT, !(Record255_1.current.standard));
		printCsvValue("MotorSpeed:LBack", Record218_241.current.lBackSpeed,
				lineT, !(Record255_1.current.standard));
		printCsvValue("MotorSpeed:RBack", Record218_241.current.rBackSpeed,
				lineT, !(Record255_1.current.standard));
		printCsvValue("MotorLoad:RFront", Record218_241.current.rFrontLoad,
				lineT, !(Record255_1.current.standard));
		printCsvValue("MotorLoad:LFront", Record218_241.current.lFrontLoad,
				lineT, !(Record255_1.current.standard));
		printCsvValue("MotorLoad:LBack", Record218_241.current.lBackLoad,
				lineT, !(Record255_1.current.standard));
		printCsvValue("MotorLoad:RBack", Record218_241.current.rBackLoad,
				lineT, !(Record255_1.current.standard));

		// Rec152_0
		printCsvValue("Control:Aileron", Record152_0.current.aileron, lineT,
				true);
		printCsvValue("Control:Elevator", Record152_0.current.elevator, lineT,
				true);
		printCsvValue("Control:Throttle", Record152_0.current.throttle, lineT,
				true);
		printCsvValue("Control:Rudder", Record152_0.current.rudder, lineT, true);
		printCsvValue("Control:ModeSwitch", Record152_0.current.modeSwitch,
				lineT, true);

		if (ConvertDat.EXPERIMENTAL) {
			printCsvValue("ContrlLinkQual", Record152_0.current.clq, lineT,
					(Record152_0.current.clqHasValue));

		}

		printCsvValue(
				"tabletLongitude",
				Record193_43.current.longitudeTablet,
				lineT,
				(Record193_43.current.valid && Record42_12.current.flyc_state == 25));
		printCsvValue(
				"tabletLatitude",
				Record193_43.current.latitudeTablet,
				lineT,
				(Record193_43.current.valid && Record42_12.current.flyc_state == 25));

		// Rec92_3
		if (ConvertDat.EXPERIMENTAL) {
			printCsvValue("errorStatus", Record92_3.current.errorStatus, lineT,
					true);
		}

		String model = "";
		if (Record255_1.current.P3) {
			model = "P3Adv/Pro";
		} else if (Record255_1.current.inspire1) {
			model = "Inspire1";
		} else if (Record255_1.current.standard) {
			model = "P3Standard";
		}

		printCsvValue("A/C model", model, lineT, true);
		if (csvEventLogOutput) {
			String noComma = Record255_1.current.text.replaceAll(",", ".");
			printCsvValue("eventLog", noComma, lineT, true);
			Record255_1.current.text = "";
		}
		if (printVersion) {
			printCsvValue(version, "", lineT, false);
		}
		_csv.println();
		if (lineT == lineType.LINE) {
			notFirstLine = true;
			lastTickNo = tickNo;
		}
	}

	public void setRecords(Record[] recs) {
		records = recs;
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void createRecords() {
		ArrayList<Record> recordsList = new ArrayList<Record>();
		recordsList = new ArrayList<Record>();
		recordsList.add(new Record207());
		recordsList.add(new Record42_12(this));
		recordsList.add(new Record68_17());
		recordsList.add(new Record44_52());
		recordsList.add(new Record218_241());
		recordsList.add(new Record152_0());
		recordsList.add(new Record30_18());
		recordsList.add(new Record198_13());
		recordsList.add(new Record193_43());
		recordsList.add(new Record92_3());
		// recordsList.add(new Record187_7());
		recordsList.add(new Record255_1(this));
		recordsList.add(new Record255_2(this));

		records = new Record[recordsList.size()];
		for (int i = 0; i < recordsList.size(); i++) {
			records[i] = recordsList.get(i);
		}
	}

}
