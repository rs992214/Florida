package com.thinkware.florida.external.service;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 파일명 필터
 */

public class PortFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        return name.startsWith("ttyUSB");
    }
}
