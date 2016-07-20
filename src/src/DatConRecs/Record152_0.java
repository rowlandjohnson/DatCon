/* Record152_0 class

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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import src.Files.ConvertDat;
import src.Files.FileEnd;

public class Record152_0 extends Record {
    // 50 hZ
    // length 45

    public static Record152_0 current = null;

    public short aileron = 0;

    public short elevator = 0;

    public short throttle = 0;

    public short rudder = 0;

    public byte modeSwitch = 0;

    public short gpsHealth = 0;

    Vector<Long> hits = new Vector<Long>(500);

    Iterator<Long> hitsIterator = null;

    private int numHits = 0;

    public double clq = 0.0;

    public boolean clqHasValue = false;

    public Record152_0() {
        _type = 152;
        _subType = 0;
        current = this;
    }

    public void process(Payload _payload) {
        super.process(_payload);
        aileron = payloadBB.getShort(4);
        elevator = payloadBB.getShort(6);
        throttle = payloadBB.getShort(8);
        rudder = payloadBB.getShort(10);
        modeSwitch = payloadBB.get(31);
        gpsHealth = payloadBB.get(41);
        if (ConvertDat.EXPERIMENTAL) {
            hits.add(0, new Long(_payload.tickNo));
            if (numHits == 500) {
                hits.remove(499);
                int count = 0;
                hitsIterator = hits.iterator();
                while (hitsIterator.hasNext()) {
                    long ti = ((Long) hitsIterator.next()).longValue();
                    if (ti < _payload.tickNo - 6000)
                        break;
                    count++;
                }
                clq = ((double) count) / numHits;
                clqHasValue = true;
            } else
                numHits++;
        }
    }
}

