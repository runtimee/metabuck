/*
 * Copyright 2012-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a platform to target for Android. Eventually, it should be possible to construct an
 * arbitrary platform target, but currently, we only recognize a fixed set of targets.
 */
public class AndroidPlatformTarget {

  static final String DEFAULT_ANDROID_PLATFORM_TARGET = "Google Inc.:Google APIs:16";
  static final int FIRST_API_WITH_BUILDTOOLS = 17;

  private final String name;
  private final File androidJar;
  private final List<File> bootclasspathEntries;
  private final File aaptExecutable;
  private final File adbExecutable;
  private final File aidlExecutable;
  private final File zipalignExecutable;
  private final File dxExecutable;
  private final File androidFrameworkIdlFile;
  private final File proguardJar;
  private final File proguardConfig;
  private final File optimizedProguardConfig;

  private AndroidPlatformTarget(
      String name,
      File androidJar,
      List<File> bootclasspathEntries,
      File aaptExecutable,
      File adbExecutable,
      File aidlExecutable,
      File zipalignExecutable,
      File dxExecutable,
      File androidFrameworkIdlFile,
      File proguardJar,
      File proguardConfig,
      File optimizedProguardConfig) {
    this.name = Preconditions.checkNotNull(name);
    this.androidJar = Preconditions.checkNotNull(androidJar);
    this.bootclasspathEntries = ImmutableList.copyOf(bootclasspathEntries);
    this.aaptExecutable = Preconditions.checkNotNull(aaptExecutable);
    this.adbExecutable = Preconditions.checkNotNull(adbExecutable);
    this.aidlExecutable = Preconditions.checkNotNull(aidlExecutable);
    this.zipalignExecutable = Preconditions.checkNotNull(zipalignExecutable);
    this.dxExecutable = Preconditions.checkNotNull(dxExecutable);
    this.androidFrameworkIdlFile = Preconditions.checkNotNull(androidFrameworkIdlFile);
    this.proguardJar = Preconditions.checkNotNull(proguardJar);
    this.proguardConfig = Preconditions.checkNotNull(proguardConfig);
    this.optimizedProguardConfig = Preconditions.checkNotNull(optimizedProguardConfig);
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
  }

  public File getAndroidJar() {
    return androidJar;
  }

  public List<File> getBootclasspathEntries() {
    return bootclasspathEntries;
  }

  public File getAaptExecutable() {
    return aaptExecutable;
  }

  public File getAdbExecutable() {
    return adbExecutable;
  }

  public File getAidlExecutable() {
    return aidlExecutable;
  }

  public File getZipalignExecutable() {
    return zipalignExecutable;
  }

  public File getDxExecutable() {
    return dxExecutable;
  }

  public File getAndroidFrameworkIdlFile() {
    return androidFrameworkIdlFile;
  }

  public File getProguardJar() {
    return proguardJar;
  }

  public File getProguardConfig() {
    return proguardConfig;
  }

  public File getOptimizedProguardConfig() {
    return optimizedProguardConfig;
  }

  /**
   * @param platformId for the platform, such as "Google Inc.:Google APIs:16"
   * @param androidSdkDir directory where the user's Android SDK is installed
   */
  public static Optional<AndroidPlatformTarget> getTargetForId(
      String platformId,
      File androidSdkDir) {
    Preconditions.checkNotNull(platformId);
    Preconditions.checkNotNull(androidSdkDir);

    Pattern platformPattern = Pattern.compile("Google Inc\\.:Google APIs:(\\d+)");
    Matcher platformMatcher = platformPattern.matcher(platformId);
    if (platformMatcher.matches()) {
      try {
        int apiLevel = Integer.parseInt(platformMatcher.group(1));
        return Optional.of(new AndroidWithGoogleApisFactory().newInstance(androidSdkDir, apiLevel));
      } catch (NumberFormatException e) {
        return Optional.absent();
      }
    } else {
      return Optional.absent();
    }
  }

  public static AndroidPlatformTarget getDefaultPlatformTarget(File androidSdkDir) {
    return getTargetForId(DEFAULT_ANDROID_PLATFORM_TARGET, androidSdkDir).get();
  }

  private static interface Factory {
    public AndroidPlatformTarget newInstance(File androidSdkDir, int apiLevel);
  }

  /**
   * Resolves all of the jarPaths against the androidSdkDir path.
   * @return a mutable list
   */
  private static LinkedList<File> resolvePaths(final File androidSdkDir, Set<String> jarPaths) {
    return Lists.newLinkedList(Iterables.transform(jarPaths, new Function<String, File>() {
      @Override
      public File apply(String jarPath) {
        File jar = new File(androidSdkDir, jarPath);
        if (!jar.isFile()) {
          throw new RuntimeException("File not found: " + jar.getAbsolutePath());
        }
        return jar;
      }
    }));
  }

  /**
   * Given the path to the Android SDK as well as the platform path within the Android SDK,
   * find all the files needed to create the {@link AndroidPlatformTarget}, assuming that the
   * organization of the Android SDK conforms to the ordinary directory structure.
   */
  @VisibleForTesting
  static AndroidPlatformTarget createFromDefaultDirectoryStructure(
      String name,
      File androidSdkDir,
      String platformDirectoryPath,
      Set<String> additionalJarPaths) {
    File platformDirectory = new File(androidSdkDir, platformDirectoryPath);
    File androidJar = new File(platformDirectory, "android.jar");
    LinkedList<File> bootclasspathEntries = resolvePaths(androidSdkDir, additionalJarPaths);
    File androidFrameworkIdlFile = new File(platformDirectory, "framework.aidl");
    File proguardJar = new File(androidSdkDir, "tools/proguard/lib/proguard.jar");
    File proguardConfig = new File(androidSdkDir, "tools/proguard/proguard-android.txt");
    File optimizedProguardConfig =
        new File(androidSdkDir, "tools/proguard/proguard-android-optimize.txt");

    // Make sure android.jar is at the front of the bootclasspath.
    bootclasspathEntries.addFirst(androidJar);

    // TODO(royw): I don't know what the long term plan is for this directory layout.  Improve these
    // heuristics when we do.
    String buildToolsDir = "platform-tools";
    if (new File(androidSdkDir, "build-tools").exists()) {
      // If the user has installed an SDK that has a build-tools directory, use the first version
      // found.
      buildToolsDir = String.format("build-tools/%d.0.0", FIRST_API_WITH_BUILDTOOLS);
    }

    return new AndroidPlatformTarget(
        name,
        androidJar,
        bootclasspathEntries,
        new File(androidSdkDir, buildToolsDir + "/aapt"),
        new File(androidSdkDir, "platform-tools/adb"),
        new File(androidSdkDir, buildToolsDir + "/aidl"),
        new File(androidSdkDir, "tools/zipalign"),
        new File(androidSdkDir, buildToolsDir + "/dx"),
        androidFrameworkIdlFile,
        proguardJar,
        proguardConfig,
        optimizedProguardConfig);
  }

  /**
   * Factory to build an AndroidPlatformTarget that corresponds to a given Google API level.
   */
  private static class AndroidWithGoogleApisFactory implements Factory {

    @Override
    public AndroidPlatformTarget newInstance(File androidSdkDir, int apiLevel) {
      String addonPath = String.format("/add-ons/addon-google_apis-google-%d/libs/", apiLevel);
      File addonDirectory = new File(androidSdkDir.getPath() + addonPath);
      String[] addonFiles = addonDirectory.list(new AddonFilter());

      if (addonFiles == null || addonFiles.length == 0) {
        throw new HumanReadableException(
            "Google APIs not found in %s.\n" +
            "Please run '$ANDROID_SDK/tools/android sdk' and select both 'SDK Platform' and " +
            "'Google APIs' under Android (API %d)",
            addonDirectory.getAbsolutePath(),
            apiLevel);
      }

      ImmutableSet.Builder<String> builder = ImmutableSet.builder();

      for (String filename: addonFiles) {
        builder.add(addonPath + filename);
      }
      Set<String> ADDITIONAL_JAR_PATHS = builder.build();

      return createFromDefaultDirectoryStructure(
          String.format("Google Inc.:Google APIs:%d", apiLevel),
          androidSdkDir,
          String.format("platforms/android-%d", apiLevel),
          ADDITIONAL_JAR_PATHS);
    }
  }

  private static class AddonFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(".jar");
    }
  }
}
