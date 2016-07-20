/* Payload class

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

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import src.Files.DatFile;
import src.Files.FileEnd;
import src.Files.Quaternion;
import src.Files.RollPitchYaw;

public class Payload {

    byte xorArray[] = null;

    ByteBuffer BB = null;

    int length = 0;

    int recType = 0;

    public long tickNo = 0;

    long start = 0;

    short subType = 0;

    public DatFile datFile = null;

    public Payload(DatFile df, long _start, int _length, int _segtype,
            short _subType, long _tickNo) throws IOException, FileEnd {
        datFile = df;
        start = _start;
        length = _length;
        recType = _segtype;
        tickNo = _tickNo;
        subType = _subType;

        xorArray = new byte[length];
        BB = ByteBuffer.wrap(xorArray).order(ByteOrder.LITTLE_ENDIAN);

        byte xorKey = (byte) (tickNo % 256);
        for (int i = 0; i < length; i++) {
            if (start + i >= datFile.getLength()) {
                throw (new FileEnd());
            }
            datFile.setPosition(start + i);
            xorArray[i] = (byte) (datFile.getByte() ^ xorKey);
        }
    }

    public long getStart() {
        return start;
    }

    public ByteBuffer getBB() {
        return BB;
    }

    public short subType() {
        return subType;
    }

    public void printBB(PrintStream printStream) {
        printStream.print("Rec" + recType + "_" + subType + " ");
        printStream.print("FilePos = " + start + " ");
        if (tickNo >= 0)
            printStream.print("TickNo = " + tickNo);
        printStream.println("");
        for (int i = 0; i < length; i++) {
            printStream.print(i + ":"
                    + String.format("%02X", (0xff & BB.get(i))) + ":"
                    + String.format("%C", (0xff & BB.get(i))) + ":"
                    + (0xff & BB.get(i)));
            if (i < length - 1) {
                printStream.print(":Shrt " + BB.getShort(i) + " :UShrt "
                        + getUnsignedShort(i));
            }
            if (i < length - 3) {
                printStream.print(" :I " + BB.getInt(i) + " :UI "
                        + getUnsignedInt(i) + " :F " + BB.getFloat(i));
            }
            if (i < length - 7) {
                printStream.print(" :L " + BB.getLong(i) + " :D "
                        + BB.getDouble(i));
            }
            printStream.println("");
        }
    }

    public short getByte(int index) {
        return BB.get(index);
    }

    public short getUnsignedByte(int index) {
        return (short) (0xff & BB.get(index));
    }

    public long getUnsignedInt(int index) {
        return (long) (0xff & BB.get(index))
                + (256 * (long) (0xff & BB.get(index + 1)))
                + (65536 * (long) (0xff & BB.get(index + 2)))
                + (65536 * 256 * (long) (0xff & BB.get(index + 3)));
    }

    public int getUnsignedShort(int index) {
        return (int) (0xff & BB.get(index))
                + (256 * (int) (0xff & BB.get(index + 1)));
    }

    public float getFloat(int index) {
        return BB.getFloat(index);
    }

    public int getShort(int index) {
        return BB.getShort(index);
    }

    public double getDouble(int index) {
        return BB.getDouble(index);
    }

    public long getTickNo() {
        return tickNo;
    }

 

    public String getString() {
        byte bytes[] = new byte[length];
        int l = 0;
        byte B = 0x00;
        for (int i = 0; i < length; i++) {
            B = BB.get(i);
            if (B == 0x00 || B == '\r' || B == '\n') {
                l = i;
                break;
            }
            bytes[i] = B;
        }
        String retv = new String(bytes, 0, l);
        return retv;
    }

    public void printBB() {
        printBB(System.out);
    }

 
}
