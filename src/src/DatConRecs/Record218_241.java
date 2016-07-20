/* Record218_241 class

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

public class Record218_241 extends Record {
    
    public static Record218_241 current = null;

    boolean valid = false;

    public short rFrontLoad = 0;

    public short lFrontLoad = 0;

    public short lBackLoad = 0;

    public short rBackLoad = 0;

    public short rFrontSpeed = 0;

    public short lFrontSpeed = 0;

    public short lBackSpeed = 0;

    public short rBackSpeed = 0;

    public Record218_241() {
        _type = 218;
        _subType = 241;
        current = this;
    }

    // Byte 0, 19, 38, 57 may be ESC , usually 0, but -1 with ESC error

    public void process(Payload _payload) {
        super.process(_payload);
        rFrontLoad = payloadBB.getShort(1);
        rFrontSpeed = payloadBB.getShort(3);
        lFrontLoad = payloadBB.getShort(20);
        lFrontSpeed = payloadBB.getShort(22);
        lBackLoad = payloadBB.getShort(39);
        lBackSpeed = payloadBB.getShort(41);
        rBackLoad = payloadBB.getShort(58);
        rBackSpeed = payloadBB.getShort(60);
    }

}
