/* Record255_2 class

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

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.Files.ConvertDat;


public class Record255_2 extends Record {

    public static Record255_2 current = null;

    ByteBuffer payload = null;

    private ConvertDat convertDat = null;

    public int batteryCycleCount = 0;

    public int batteryPercentage = 0;

    Pattern numberPattern = Pattern.compile("\\[\\s*(\\d+)\\s*\\]");

    public Record255_2(ConvertDat convertDat) {
        current = this;
        _type = 255;
        _subType = 2;
        this.convertDat = convertDat;
    }

    public void process(Payload _payload) {
        super.process(_payload);
        payload = _payload.getBB();
        String payloadString = _payload.getString();
        if (payloadString.length() > 0) {
            if (convertDat.cloPS != null) {
                convertDat.cloPS.println(+_payload.getTickNo() + " : "
                        + payloadString);
            }
            if (payloadString.indexOf("battery_info.life_percentage_0") > -1) {
                Matcher numberMatcher = numberPattern.matcher(payloadString);
                if (numberMatcher.find()) {
                    String number = payloadString.substring(
                            numberMatcher.start(1), numberMatcher.end(1));
                    batteryPercentage = Integer.parseInt(number);
                }
            }
            if (payloadString.indexOf("battery_info.cycle_count_0") > -1) {
                Matcher numberMatcher = numberPattern.matcher(payloadString);
                if (numberMatcher.find()) {
                    String number = payloadString.substring(
                            numberMatcher.start(1), numberMatcher.end(1));
                    batteryCycleCount = Integer.parseInt(number);
                }
            }
        }
    }

}
