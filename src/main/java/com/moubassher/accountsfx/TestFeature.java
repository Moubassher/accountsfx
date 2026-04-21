package com.moubassher.accountsfx;

enum TestFeature implements Feature<TestFeature> {
    All, FeatureA, FeatureB, Sale_Approval;

    @Override
    public TestFeature saleApproval() {
        return Sale_Approval;
    }
}
