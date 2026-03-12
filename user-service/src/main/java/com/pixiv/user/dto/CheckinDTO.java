package com.pixiv.user.dto;

import java.util.List;

public class CheckinDTO {

    private boolean checkedInToday;
    private int consecutiveDays;
    private int pointsEarned;
    private int totalPoints;
    private int availablePoints;
    private String membershipLevel;
    private int multiplier;
    private List<Boolean> weekCheckins;

    public CheckinDTO() {
    }

    public boolean isCheckedInToday() {
        return checkedInToday;
    }

    public void setCheckedInToday(boolean checkedInToday) {
        this.checkedInToday = checkedInToday;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(int availablePoints) {
        this.availablePoints = availablePoints;
    }

    public String getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(String membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public List<Boolean> getWeekCheckins() {
        return weekCheckins;
    }

    public void setWeekCheckins(List<Boolean> weekCheckins) {
        this.weekCheckins = weekCheckins;
    }
}
