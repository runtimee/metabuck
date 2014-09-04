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

package com.facebook.buck.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.facebook.buck.event.BuckEventBus;
import com.facebook.buck.event.BuckEventBusFactory;
import com.facebook.buck.java.FakeJavaPackageFinder;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.BuildTargetPattern;
import com.facebook.buck.parser.BuildTargetParser;
import com.facebook.buck.rules.ArtifactCache;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleFactoryParams;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildRuleType;
import com.facebook.buck.rules.ConstructorArg;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.FakeBuildRule;
import com.facebook.buck.rules.FakeRepositoryFactory;
import com.facebook.buck.rules.NonCheckingBuildRuleFactoryParams;
import com.facebook.buck.rules.NoopArtifactCache;
import com.facebook.buck.rules.Repository;
import com.facebook.buck.rules.TargetNode;
import com.facebook.buck.rules.TestRepositoryBuilder;
import com.facebook.buck.testutil.TestConsole;
import com.facebook.buck.util.AndroidDirectoryResolver;
import com.facebook.buck.util.FakeAndroidDirectoryResolver;
import com.facebook.buck.util.MorePaths;
import com.facebook.buck.util.ProjectFilesystem;
import com.facebook.buck.util.environment.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Outputs targets that own a specified list of files.
 */
public class AuditOwnerCommandTest {
  private TestConsole console;

  public static class FakeDescription implements Description<FakeDescription.FakeArg> {

    @Override
    public BuildRuleType getBuildRuleType() {
      return new BuildRuleType("fake_rule");
    }

    @Override
    public FakeArg createUnpopulatedConstructorArg() {
      return new FakeArg();
    }

    @Override
    public <A extends FakeArg> BuildRule createBuildRule(
        BuildRuleParams params,
        BuildRuleResolver resolver,
        A args) {
      return new FakeBuildRule(params);
    }

    public static class FakeArg implements ConstructorArg {

    }
  }

  private static TargetNode<?> createTargetNode(
      BuildTarget buildTarget,
      ImmutableSet<Path> inputs) {
    Description<FakeDescription.FakeArg> description = new FakeDescription();
    BuildRuleFactoryParams params =
        NonCheckingBuildRuleFactoryParams.createNonCheckingBuildRuleFactoryParams(
            Maps.<String, Object>newHashMap(),
            new BuildTargetParser(new FakeProjectFilesystem()),
            buildTarget);
    return new TargetNode<>(
        description,
        params,
        inputs,
        ImmutableSet.<BuildTarget>of(),
        ImmutableSet.<BuildTargetPattern>of());
  }

  private static class FakeProjectFilesystem extends ProjectFilesystem {
    public FakeProjectFilesystem() {
      super(new File("."));
    }
  }

  @SuppressWarnings("serial")
  private static class ExistingDirectoryFile extends File {
    public ExistingDirectoryFile(Path file, String s) {
      super(file.toFile(), s);
    }

    @Override
    public boolean isFile() {
      return false;
    }

    @Override
    public boolean isDirectory() {
      return true;
    }

    @Override
    public boolean exists() {
      return true;
    }
  }

  @SuppressWarnings("serial")
  private static class MissingFile extends File {
    public MissingFile(Path file, String s) {
      super(file.toFile(), s);
    }

    @Override
    public boolean exists() {
      return false;
    }

    @Override
    public boolean isFile() {
      return true;
    }

    @Override
    public boolean isDirectory() {
      return false;
    }
  }

  @SuppressWarnings("serial")
  private static class ExistingFile extends File {
    public ExistingFile(Path file, String s) {
      super(file.toFile(), s);
    }

    @Override
    public boolean exists() {
      return true;
    }

    @Override
    public boolean isFile() {
      return true;
    }

    @Override
    public boolean isDirectory() {
      return false;
    }
  }

  private BuckConfig buckConfig;

  @Before
  public void setUp() {
    console = new TestConsole();
    buckConfig = new FakeBuckConfig();
  }

  private AuditOwnerCommand createAuditOwnerCommand(ProjectFilesystem filesystem)
      throws IOException, InterruptedException {
    ArtifactCache artifactCache = new NoopArtifactCache();
    BuckEventBus eventBus = BuckEventBusFactory.newInstance();
    AndroidDirectoryResolver androidDirectoryResolver = new FakeAndroidDirectoryResolver();
    Repository repository = new TestRepositoryBuilder().setFilesystem(filesystem).build();
    return new AuditOwnerCommand(new CommandRunnerParams(
        console,
        new FakeRepositoryFactory(),
        repository,
        androidDirectoryResolver,
        new InstanceArtifactCacheFactory(artifactCache),
        eventBus,
        buckConfig.getPythonInterpreter(),
        Platform.detect(),
        ImmutableMap.copyOf(System.getenv()),
        new FakeJavaPackageFinder(),
        new ObjectMapper()));
  }

  @Test
  public void verifyPathsThatAreNotFilesAreCorrectlyReported()
      throws CmdLineException, IOException, InterruptedException {
    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      @Override
      public File getFileForRelativePath(String pathRelativeToProjectRoot) {
        return new ExistingDirectoryFile(getRootPath(), pathRelativeToProjectRoot);
      }
    };

    // Inputs that should be treated as "non-files", i.e. as directories
    ImmutableSet<String> inputs = ImmutableSet.of(
        "java/somefolder/badfolder",
        "java/somefolder",
        "com/test/subtest");

    BuildTarget target = BuildTarget.builder("//base", "name").build();
    TargetNode<?> targetNode = createTargetNode(target, ImmutableSet.<Path>of());

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    AuditOwnerCommand.OwnersReport report = command.generateOwnersReport(targetNode, inputs, false);
    assertTrue(report.owners.isEmpty());
    assertTrue(report.nonExistentInputs.isEmpty());
    assertTrue(report.inputsWithNoOwners.isEmpty());
    assertEquals(inputs, report.nonFileInputs);
  }

  @Test
  public void verifyMissingFilesAreCorrectlyReported()
      throws CmdLineException, IOException, InterruptedException {
    // All files will be directories now
    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      @Override
      public File getFileForRelativePath(String pathRelativeToProjectRoot) {
        return new MissingFile(getRootPath(), pathRelativeToProjectRoot);
      }
    };

    // Inputs that should be treated as missing files
    ImmutableSet<String> inputs = ImmutableSet.of(
        "java/somefolder/badfolder/somefile.java",
        "java/somefolder/perfect.java",
        "com/test/subtest/random.java");

    BuildTarget target = BuildTarget.builder("//base", "name").build();
    TargetNode<?> targetNode = createTargetNode(target, ImmutableSet.<Path>of());

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    AuditOwnerCommand.OwnersReport report = command.generateOwnersReport(targetNode, inputs, false);
    assertTrue(report.owners.isEmpty());
    assertTrue(report.nonFileInputs.isEmpty());
    assertTrue(report.inputsWithNoOwners.isEmpty());
    assertEquals(inputs, report.nonExistentInputs);
  }

  @Test
  public void verifyInputsWithoutOwnersAreCorrectlyReported()
      throws CmdLineException, IOException, InterruptedException {
    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      @Override
      public File getFileForRelativePath(String pathRelativeToProjectRoot) {
        return new ExistingFile(getRootPath(), pathRelativeToProjectRoot);
      }
    };

    // Inputs that should be treated as existing files
    ImmutableSet<String> inputs = ImmutableSet.of(
        "java/somefolder/badfolder/somefile.java",
        "java/somefolder/perfect.java",
        "com/test/subtest/random.java");
    ImmutableSet<Path> inputPaths = MorePaths.asPaths(inputs);

    BuildTarget target = BuildTarget.builder("//base", "name").build();
    TargetNode<?> targetNode = createTargetNode(target, ImmutableSet.<Path>of());

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    AuditOwnerCommand.OwnersReport report = command.generateOwnersReport(targetNode, inputs, false);
    assertTrue(report.owners.isEmpty());
    assertTrue(report.nonFileInputs.isEmpty());
    assertTrue(report.nonExistentInputs.isEmpty());
    assertEquals(inputPaths, report.inputsWithNoOwners);
  }

  /**
   * Verify that owners are correctly detected:
   *  - one owner, multiple inputs
   */
  @Test
  public void verifyInputsWithOneOwnerAreCorrectlyReported()
      throws CmdLineException, IOException, InterruptedException {
    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      @Override
      public File getFileForRelativePath(String pathRelativeToProjectRoot) {
        return new ExistingFile(getRootPath(), pathRelativeToProjectRoot);
      }
    };

    ImmutableSet<String> inputs = ImmutableSet.of(
        "java/somefolder/badfolder/somefile.java",
        "java/somefolder/perfect.java",
        "com/test/subtest/random.java");
    ImmutableSet<Path> inputPaths = MorePaths.asPaths(inputs);

    BuildTarget target = BuildTarget.builder("//base", "name").build();
    TargetNode<?> targetNode = createTargetNode(target, inputPaths);

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    AuditOwnerCommand.OwnersReport report = command.generateOwnersReport(targetNode, inputs, false);
    assertTrue(report.nonFileInputs.isEmpty());
    assertTrue(report.nonExistentInputs.isEmpty());
    assertTrue(report.inputsWithNoOwners.isEmpty());

    assertEquals(inputs.size(), report.owners.size());
    assertTrue(report.owners.containsKey(targetNode));
    assertEquals(targetNode.getInputs(), report.owners.get(targetNode));
  }

  /**
   * Verify that owners are correctly detected:
   *  - one owner, multiple inputs, json output
   */
  @Test
  public void verifyInputsWithOneOwnerAreCorrectlyReportedInJson()
      throws CmdLineException, IOException, InterruptedException {
    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      @Override
      public File getFileForRelativePath(String pathRelativeToProjectRoot) {
        return new ExistingFile(getRootPath(), pathRelativeToProjectRoot);
      }
    };

    ImmutableSet<String> inputs = ImmutableSet.of(
        "java/somefolder/badfolder/somefile.java",
        "java/somefolder/perfect.java",
        "com/test/subtest/random.java");
    ImmutableSortedSet<Path> inputPaths = MorePaths.asPaths(inputs);

    BuildTarget target = BuildTarget.builder("//base/name", "name").build();
    TargetNode<?> targetNode = createTargetNode(target, inputPaths);

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    AuditOwnerCommand.OwnersReport report = command.generateOwnersReport(targetNode, inputs, false);
    command.printOwnersOnlyJsonReport(report);

    String expectedJson = Joiner.on("").join(
      "{",
      "\"com/test/subtest/random.java\":[\"//base/name:name\"],",
      "\"java/somefolder/badfolder/somefile.java\":[\"//base/name:name\"],",
      "\"java/somefolder/perfect.java\":[\"//base/name:name\"]",
      "}"
    );

    assertEquals(expectedJson, console.getTextWrittenToStdOut());
    assertEquals("", console.getTextWrittenToStdErr());
  }

  /**
   * Verify that owners are correctly detected:
   *  - inputs that belong to multiple targets
   */
  @Test
  public void verifyInputsWithMultipleOwnersAreCorrectlyReported()
      throws CmdLineException, IOException, InterruptedException {
    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      @Override
      public File getFileForRelativePath(String pathRelativeToProjectRoot) {
        return new ExistingFile(getRootPath(), pathRelativeToProjectRoot);
      }
    };

    ImmutableSet<String> inputs = ImmutableSet.of(
        "java/somefolder/badfolder/somefile.java",
        "java/somefolder/perfect.java",
        "com/test/subtest/random.java");
    ImmutableSortedSet<Path> inputPaths = MorePaths.asPaths(inputs);

    BuildTarget target1 = BuildTarget.builder("//base/name1", "name").build();
    BuildTarget target2 = BuildTarget.builder("//base/name2", "name").build();
    TargetNode<?> targetNode1 = createTargetNode(target1, inputPaths);
    TargetNode<?> targetNode2 = createTargetNode(target2, inputPaths);

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    AuditOwnerCommand.OwnersReport report = AuditOwnerCommand.OwnersReport.emptyReport();
    report = report.updatedWith(command.generateOwnersReport(targetNode1, inputs, false));
    report = report.updatedWith(command.generateOwnersReport(targetNode2, inputs, false));

    assertTrue(report.nonFileInputs.isEmpty());
    assertTrue(report.nonExistentInputs.isEmpty());
    assertTrue(report.inputsWithNoOwners.isEmpty());

    assertTrue(report.owners.containsKey(targetNode1));
    assertTrue(report.owners.containsKey(targetNode2));
    assertEquals(targetNode1.getInputs(), report.owners.get(targetNode1));
    assertEquals(targetNode2.getInputs(), report.owners.get(targetNode2));
  }

  @Test
  public void verifyBuckFileIsFoundWhenAvailable() throws IOException, InterruptedException {
    final Path root = Paths.get("root");
    final Path buckFile = Paths.get("root/BUCK");

    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      private ImmutableSet<Path> existingFiles = ImmutableSet.of(
          buckFile,
          Paths.get("root/.buckconfig"),
          Paths.get("root/sub/Test.java"));

      @Override
      public boolean exists(Path path) {
        return existingFiles.contains(path);
      }

      @Override
      public Path resolve(Path path) {
        if (path.equals(Paths.get(".buckconfig"))) {
          return Paths.get("root/.buckconfig");
        }
        return path;
      }

      @Override
      public boolean isDirectory(Path path, LinkOption... linkOptions) {
        return path.equals(root) || path.equals(Paths.get("root/sub"));
      }

      @Override
      public Path getRootPath() {
        return root;
      }
    };

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    assertEquals(
        buckFile,
        command.findBuckFileFor(Paths.get("root/.buckconfig")));
    assertEquals(
        buckFile,
        command.findBuckFileFor(Paths.get(".buckconfig")));
    assertEquals(
        buckFile,
        command.findBuckFileFor(Paths.get("root/sub/Test.java")));
  }

  @Test
  public void verifyBuckFileIsNullWhenNotFound() throws IOException, InterruptedException {
    FakeProjectFilesystem filesystem = new FakeProjectFilesystem() {
      @Override
      public boolean exists(Path path) {
        return path.equals(Paths.get("root/.buckconfig"));
      }

      @Override
      public boolean isDirectory(Path path, LinkOption... linkOptions) {
        return path.equals(getRootPath());
      }

      @Override
      public Path getRootPath() {
        return Paths.get("root");
      }
    };

    AuditOwnerCommand command = createAuditOwnerCommand(filesystem);
    assertEquals(
        null,
        command.findBuckFileFor(Paths.get("root/.buckconfig")));
  }

}
