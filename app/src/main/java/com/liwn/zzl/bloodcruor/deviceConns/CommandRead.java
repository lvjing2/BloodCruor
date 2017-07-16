package com.liwn.zzl.bloodcruor.deviceConns;

/**
 * Created by zzl on 17/7/12.
 */

public class CommandRead {
    private int index;
    private int packageCnt;
    private byte[] sendBytes;

    public CommandRead(int index, int packageIndex) {
        this.index = index;
        this.packageCnt = Constants.packageCntMap.get(index);
        this.sendBytes = new byte[Constants.packageAllLen];
        this.sendBytes[0] = (byte) 0xAA;
        this.sendBytes[1] = (byte) index;
        this.sendBytes[2] = (byte) packageIndex;
        this.sendBytes[Constants.packageAllLen - 1] = Constants.calculateCheckSum(this.sendBytes);
    }

    public void setPackageIndex(int packageIndex) {
        this.sendBytes[2] = (byte) packageIndex;
        this.sendBytes[Constants.packageAllLen - 1] = Constants.calculateCheckSum(this.sendBytes);
    }

    public byte[] getCommandContent() {
        return sendBytes;
    }

    public int getIndex() {
        return index;
    }

    public int getPackageCnt() {
        return packageCnt;
    }
}
