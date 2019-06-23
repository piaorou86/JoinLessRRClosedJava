package com.hatran;

//空间实例
public class SpaceInstance {
    private int InstanceID; // 实例编号
    private char FeatureType; // 实例所属特征
    private PositionInSpace positionInSpace; // 实例空间位置

    final public void setInstanceID(int InstanceID) {
        this.InstanceID = InstanceID;
    }

    final public int getInstanceID() {
        return this.InstanceID;
    }

    final public void setFeatureType(char FeatureType) {
        this.FeatureType = FeatureType;
    }

    final public char getFeatureType() {
        return FeatureType;
    }

    final public void setPositionInSpace(PositionInSpace positionInSpace) {
        this.positionInSpace = positionInSpace;
    }

    final public PositionInSpace getPositionInSpace() {
        return this.positionInSpace;
    }

    public void DisplaySpaceInstance() {
        System.out.print(this.InstanceID + " " + this.FeatureType + " ");
        this.positionInSpace.DisplayPositionInSpace();
        System.out.println();
    }
}