package io.github.jilipop.adohri.utils;

public class VersionProviderImpl implements VersionProvider {
    @Override
    public int getSdkInt() {
        return android.os.Build.VERSION.SDK_INT;
    }
}
