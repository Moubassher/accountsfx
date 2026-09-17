package com.moubassher.accountsfx.testApp;

import com.moubassher.accountsfx.Feature;

public enum TestFeature implements Feature<TestFeature> {
    All("كل المميزات"),
    FeatureA("الميزة ألف"),
    FeatureB("الميزة باء"),
    Sale_Approval("التصديق على عملية بيع"),
    Login("تسجيل الدخول");

    private String description;
     TestFeature(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public TestFeature saleApproval() {
        return Sale_Approval;
    }

    @Override
    public TestFeature login() {
        return Login;
    }

    @Override
    public String toString() {
        return description;
    }
}
