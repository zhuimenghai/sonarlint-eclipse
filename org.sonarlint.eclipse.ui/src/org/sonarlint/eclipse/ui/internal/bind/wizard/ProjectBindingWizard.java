/*
 * SonarLint for Eclipse
 * Copyright (C) 2015-2018 SonarSource SA
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarlint.eclipse.ui.internal.bind.wizard;

import java.util.List;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.sonarlint.eclipse.core.resource.ISonarLintProject;

public class ProjectBindingWizard extends Wizard implements INewWizard, IPageChangingListener {

  private final ProjectBindingModel model;
  private final ServerSelectionWizardPage serverSelectionWizardPage;
  private final RemoteProjectSelectionWizardPage remoteProjectSelectionWizardPage;
  private final ProjectsSelectionWizardPage projectsSelectionWizardPage;

  private ProjectBindingWizard(String title, ProjectBindingModel model) {
    super();
    this.model = model;
    setNeedsProgressMonitor(true);
    setWindowTitle(title);
    setHelpAvailable(false);
    projectsSelectionWizardPage = new ProjectsSelectionWizardPage(model);
    serverSelectionWizardPage = new ServerSelectionWizardPage(model);
    remoteProjectSelectionWizardPage = new RemoteProjectSelectionWizardPage(model);
  }

  /**
   * Should remain public for File -> New -> SonarQube Server
   * @param selectedProjects 
   */
  public ProjectBindingWizard(List<ISonarLintProject> selectedProjects) {
    this("Bind to a SonarQube or SonarCloud project", new ProjectBindingModel());
    this.model.setProjects(selectedProjects);
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    // Nothing to do
  }

  @Override
  public IWizardPage getStartingPage() {
    return serverSelectionWizardPage;
  }

  @Override
  public void addPages() {
    addPage(projectsSelectionWizardPage);
    addPage(serverSelectionWizardPage);
    addPage(remoteProjectSelectionWizardPage);
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    if (page == projectsSelectionWizardPage) {
      return serverSelectionWizardPage;
    }
    if (page == serverSelectionWizardPage) {
      return remoteProjectSelectionWizardPage;
    }
    return null;
  }

  @Override
  public IWizardPage getPreviousPage(IWizardPage page) {
    // This method is only used for the first page of a wizard,
    // because every following page remember the previous one on its own
    if (page == serverSelectionWizardPage) {
      return projectsSelectionWizardPage;
    }
    return null;
  }

  @Override
  public boolean canFinish() {
    return true;
  }

  @Override
  public boolean performFinish() {

    return true;
  }

  @Override
  public void handlePageChanging(PageChangingEvent event) {
  }
}
