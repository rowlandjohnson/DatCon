/* Record255_1 class

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

package src.DatConRecs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.Files.ConvertDat;
import src.Files.TSAGeoMag;
import src.Files.Util;

public class Record255_1 extends Record {

	public static Record255_1 current = null;

	public double longitudeHP = 0.0;

	public double latitudeHP = 0.0;

	public double longitudeHPDegrees;

	public double latitudeHPDegrees;

	public float heightHP = 0.0f;

	public boolean validHP = false;

	public static TSAGeoMag geoMag = new TSAGeoMag();

	public double declination = 0.0;

	public double inclination = 0.0;

	public String text = "";

	ConvertDat convertDat = null;

	public boolean inspire1 = false;

	public boolean standard = false;

	public String batteryBarCode = "";

	public boolean P3 = false;

	public Record255_1(ConvertDat convertDat) {
		current = this;
		_type = 255;
		_subType = 1;
		this.convertDat = convertDat;
	}

	public void process(Payload _payload) {
		super.process(_payload);
		long tickNo = _payload.getTickNo();
		long timeOffset = convertDat.timeOffset;
		String payloadString = _payload.getString();
		if (payloadString.length() > 0) {
			if (convertDat.csvEventLogOutput
					&& convertDat.tickRangeLower <= _payload.getTickNo()) {
				if (text.length() > 0)
					text += "|";
				text += payloadString;
			}
			if (convertDat.eloPS != null
					&& convertDat.tickRangeLower <= _payload.getTickNo()) {
				convertDat.eloPS.println(Util.timeString(tickNo, timeOffset)
						+ " : " + tickNo + " : " + payloadString);
			}
			if (payloadString.indexOf("Home Point") > -1) {
				Pattern latlonPattern = Pattern
						.compile(".*?(-*\\d+\\.\\d+)\\s+(-*\\d+\\.\\d+)\\s+(-*\\d+\\.\\d+)");
				Matcher latlonMatcher = latlonPattern.matcher(payloadString);
				if (latlonMatcher.find()) {
					String lat = payloadString.substring(
							latlonMatcher.start(1), latlonMatcher.end(1));
					String lon = payloadString.substring(
							latlonMatcher.start(2), latlonMatcher.end(2));
					String hei = payloadString.substring(
							latlonMatcher.start(3), latlonMatcher.end(3));
					longitudeHPDegrees = Double.parseDouble(lon);
					latitudeHPDegrees = Double.parseDouble(lat);
					declination = geoMag.getDeclination(latitudeHPDegrees,
							longitudeHPDegrees);
					inclination = geoMag.getDipAngle(latitudeHPDegrees,
							longitudeHPDegrees);
					heightHP = Float.parseFloat(hei);
					longitudeHP = Math.toRadians(longitudeHPDegrees);
					latitudeHP = Math.toRadians(latitudeHPDegrees);
					validHP = true;
				} 
			} else if (payloadString.indexOf("Battery barcode:") > -1) {
				batteryBarCode = payloadString.substring(payloadString
						.indexOf(":") + 1);
			}

			if (payloadString.indexOf("Board") > -1) {
				if (payloadString.indexOf("wm320v2") > -1) {
					P3 = true;
				} else if (payloadString.indexOf("wm610") > -1) {
					inspire1 = true;
				} else if (payloadString.indexOf("wm321") > -1) {
					standard = true;
				} else if (payloadString.indexOf("tp1406") > -1) {
					// / don't know what this is, involves landing gear
				}
			}
		}
	}

}
