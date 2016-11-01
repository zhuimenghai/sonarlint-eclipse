package org.sonarlint.eclipse.cdt.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.sonarlint.eclipse.core.configurator.ProjectConfigurationRequest;
import org.sonarlint.eclipse.core.internal.SonarLintCorePlugin;

public class CProjectConfiguratorTest {
  private CProjectConfigurator configurator;

  private BuildWrapperJsonFactory jsonFactory;
  private CCorePlugin cCorePlugin;
  private Predicate<IFile> fileValidator;
  private SonarLintCorePlugin core;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Before
  public void setUp() {
    cCorePlugin = mock(CCorePlugin.class);
    jsonFactory = mock(BuildWrapperJsonFactory.class);
    fileValidator = mock(Predicate.class);
    core = mock(SonarLintCorePlugin.class);
    configurator = new CProjectConfigurator(jsonFactory, cCorePlugin, fileValidator, core);
  }

  @Test
  public void should_configurate_projects_c_nature() throws CoreException {
    IProject project = mock(IProject.class);
    when(project.hasNature(CProjectNature.C_NATURE_ID)).thenReturn(true);
    assertThat(configurator.canConfigure(project)).isTrue();
  }

  @Test
  public void should_configure() {
    IProject project = mock(IProject.class);
    IFile file = mock(IFile.class);
    IProgressMonitor monitor = mock(IProgressMonitor.class);
    IScannerInfoProvider infoProvider = mock(IScannerInfoProvider.class);
    IScannerInfo info = mock(IScannerInfo.class);

    when(cCorePlugin.getScannerInfoProvider(project)).thenReturn(infoProvider);
    when(project.getLocation()).thenReturn(Path.fromOSString(temp.getRoot().getAbsolutePath()));
    when(project.getWorkingLocation(anyString())).thenReturn(Path.fromOSString(temp.getRoot().getAbsolutePath()));
    when(infoProvider.getScannerInformation(file)).thenReturn(info);
    when(fileValidator.test(file)).thenReturn(true);
    when(file.getProjectRelativePath()).thenReturn(Path.fromOSString("file1"));
    when(jsonFactory.create(anyMap(), anyString())).thenReturn("json");

    Map<String, String> props = new HashMap<>();

    ProjectConfigurationRequest request = new ProjectConfigurationRequest(project, Collections.singleton(file), props);

    configurator.configure(request, monitor);

    // json created
    verify(jsonFactory).create(anyMap(), eq(temp.getRoot().getAbsolutePath()));

    // json written
    assertThat(temp.getRoot().toPath().resolve("bw-outputs").resolve("build-wrapper-dump.json")).hasContent("json");

    // property created
    assertThat(props).containsOnly(entry("sonar.cfamily.build-wrapper-output", temp.getRoot().toPath().resolve("bw-outputs").toString()));

    // no errors
    verify(core, never()).error(Mockito.any(), Mockito.any());
    verify(core, never()).error(Mockito.any());
  }

  @Test
  public void do_nothing_on_complete() {
    configurator.analysisComplete(Collections.emptyMap(), mock(IProgressMonitor.class));
    verifyZeroInteractions(cCorePlugin, jsonFactory);
  }
}
