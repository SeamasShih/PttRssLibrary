package com.seamas.pttrsslibrary;

import android.content.Intent;
import android.view.ViewGroup;

public interface FloatingButtonClickListener {
    void onFloatingButtonClick(ViewGroup button, Intent intent , boolean isOpen);
}
