/* Record30_18 class

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

public class Record30_18 extends Record {

    public static Record30_18 current = null;

    // 1 Hz
    // length 79
    public int batteryUsefulTime = 0;

    public boolean validBUT = false;

    public boolean validVP = false;

    public int voltagePercent = 0;

    public Record30_18() {
        current = this;
        _type = 30;
        _subType = 18;
    }

    public void process(Payload _payload) {
        super.process(_payload);
        batteryUsefulTime = payloadBB.getShort(0);
        if (!validBUT) {
            if (batteryUsefulTime > 0)
                validBUT = true;
        }
        voltagePercent = payloadBB.get(72);
        if (!validVP) {
            if (voltagePercent > 0) {
                validVP = true;
            }
        }
    }
}
