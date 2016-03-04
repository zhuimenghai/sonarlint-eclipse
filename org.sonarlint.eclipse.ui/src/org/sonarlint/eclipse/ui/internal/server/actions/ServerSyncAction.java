package org.sonarlint.eclipse.ui.internal.server.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.sonarlint.eclipse.core.internal.server.IServer;
import org.sonarlint.eclipse.ui.internal.Messages;
import org.sonarlint.eclipse.ui.internal.SonarLintImages;
import org.sonarlint.eclipse.ui.internal.SonarLintUiPlugin;

public class ServerSyncAction extends SelectionProviderAction {
  private List<IServer> servers;
  private Shell shell;

  public ServerSyncAction(Shell shell, ISelectionProvider selectionProvider) {
    super(selectionProvider, Messages.actionSync);
    this.shell = shell;
    setImageDescriptor(SonarLintImages.SONARSYNCHRO_IMG);
  }

  @Override
  public void selectionChanged(IStructuredSelection sel) {
    if (sel.isEmpty()) {
      setEnabled(false);
      return;
    }
    servers = new ArrayList<>();
    boolean enabled = false;
    Iterator iterator = sel.iterator();
    while (iterator.hasNext()) {
      Object obj = iterator.next();
      if (obj instanceof IServer) {
        IServer server = (IServer) obj;
        servers.add(server);
        enabled = true;
      } else {
        setEnabled(false);
        return;
      }
    }
    setEnabled(enabled);
  }

  @Override
  public void run() {
    // It is possible that the server is created and added to the server view on workbench
    // startup. As a result, when the user switches to the server view, the server is
    // selected, but the selectionChanged event is not called, which results in servers
    // being null. When servers is null the server will not be deleted and the error log
    // will have an IllegalArgumentException.
    //
    // To handle the case where servers is null, the selectionChanged method is called
    // to ensure servers will be populated.
    if (servers == null) {

      IStructuredSelection sel = getStructuredSelection();
      if (sel != null) {
        selectionChanged(sel);
      }
    }

    if (servers != null) {
      for (final IServer server : servers) {
        Job j = new Job("Sync SonarQube server " + server.getName()) {

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            try {
              server.sync(monitor);
              return Status.OK_STATUS;
            } catch (Exception e) {
              return new Status(IStatus.ERROR, SonarLintUiPlugin.PLUGIN_ID, "Unable to sync server " + server.getName(), e);
            }
          }
        };
        j.schedule();
      }
    }
  }

}