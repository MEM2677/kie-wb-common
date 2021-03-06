/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.screens.library.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.common.services.project.context.ProjectContext;
import org.guvnor.common.services.project.context.ProjectContextChangeEvent;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.library.api.LibraryPreferences;
import org.kie.workbench.common.screens.library.api.LibraryService;
import org.kie.workbench.common.screens.library.api.ProjectInfo;
import org.kie.workbench.common.screens.library.client.events.AssetDetailEvent;
import org.kie.workbench.common.screens.library.client.events.ProjectDetailEvent;
import org.kie.workbench.common.screens.library.client.perspective.LibraryPerspective;
import org.kie.workbench.common.screens.library.client.resources.i18n.LibraryConstants;
import org.kie.workbench.common.screens.library.client.widgets.LibraryToolbarPresenter;
import org.kie.workbench.common.widgets.client.handlers.NewResourceSuccessEvent;
import org.kie.workbench.common.workbench.client.docks.AuthoringWorkbenchDocks;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.PlaceStatus;
import org.uberfire.client.workbench.events.PlaceGainFocusEvent;
import org.uberfire.ext.editor.commons.client.event.ConcurrentRenameAcceptedEvent;
import org.uberfire.ext.widgets.common.client.breadcrumbs.UberfireBreadcrumbs;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.ConditionalPlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;

@ApplicationScoped
public class LibraryPlaces {

    public static final String LIBRARY_PERSPECTIVE = "LibraryPerspective";
    public static final String NEW_PROJECT_SCREEN = "NewProjectScreen";
    public static final String EMPTY_LIBRARY_SCREEN = "EmptyLibraryScreen";
    public static final String LIBRARY_SCREEN = "LibraryScreen";
    public static final String EMPTY_PROJECT_SCREEN = "EmptyProjectScreen";
    public static final String PROJECT_SCREEN = "ProjectScreen";
    public static final String PROJECT_DETAIL_SCREEN = "ProjectsDetailScreen";
    public static final String PROJECT_SETTINGS = "projectScreen";
    public static final String PROJECT_EXPLORER = "org.kie.guvnor.explorer";

    public static final List<String> LIBRARY_PLACES = Collections.unmodifiableList(new ArrayList<String>(7) {{
        add(NEW_PROJECT_SCREEN);
        add(EMPTY_LIBRARY_SCREEN);
        add(LIBRARY_SCREEN);
        add(EMPTY_PROJECT_SCREEN);
        add(PROJECT_SCREEN);
        add(PROJECT_DETAIL_SCREEN);
        add(PROJECT_SETTINGS);
        add(PROJECT_EXPLORER);
    }});

    private UberfireBreadcrumbs breadcrumbs;

    private TranslationService ts;

    private Event<ProjectDetailEvent> projectDetailEvent;

    private Event<AssetDetailEvent> assetDetailEvent;

    private ResourceUtils resourceUtils;

    private Caller<LibraryService> libraryService;

    private PlaceManager placeManager;

    private LibraryPerspective libraryPerspective;

    private ProjectContext projectContext;

    private LibraryToolbarPresenter libraryToolbar;

    private AuthoringWorkbenchDocks docks;

    private LibraryPreferences libraryPreferences;

    private Event<ProjectContextChangeEvent> projectContextChangeEvent;

    private ExamplesUtils examplesUtils;

    private boolean docksReady = false;

    private boolean docksHidden = true;

    @Inject
    public LibraryPlaces(final UberfireBreadcrumbs breadcrumbs,
                         final TranslationService ts,
                         final Event<ProjectDetailEvent> projectDetailEvent,
                         final Event<AssetDetailEvent> assetDetailEvent,
                         final ResourceUtils resourceUtils,
                         final Caller<LibraryService> libraryService,
                         final PlaceManager placeManager,
                         final LibraryPerspective libraryPerspective,
                         final ProjectContext projectContext,
                         final LibraryToolbarPresenter libraryToolbar,
                         final AuthoringWorkbenchDocks docks,
                         final LibraryPreferences libraryPreferences,
                         final Event<ProjectContextChangeEvent> projectContextChangeEvent,
                         final ExamplesUtils examplesUtils) {
        this.breadcrumbs = breadcrumbs;
        this.ts = ts;
        this.projectDetailEvent = projectDetailEvent;
        this.assetDetailEvent = assetDetailEvent;
        this.resourceUtils = resourceUtils;
        this.libraryService = libraryService;
        this.placeManager = placeManager;
        this.libraryPerspective = libraryPerspective;
        this.projectContext = projectContext;
        this.libraryToolbar = libraryToolbar;
        this.docks = docks;
        this.libraryPreferences = libraryPreferences;
        this.projectContextChangeEvent = projectContextChangeEvent;
        this.examplesUtils = examplesUtils;
    }

    public ProjectInfo getProjectInfo() {
        return new ProjectInfo(projectContext.getActiveOrganizationalUnit(),
                               projectContext.getActiveRepository(),
                               projectContext.getActiveBranch(),
                               projectContext.getActiveProject());
    }

    public void onSelectPlaceEvent(@Observes final PlaceGainFocusEvent placeGainFocusEvent) {
        if (placeManager.getStatus(LIBRARY_PERSPECTIVE).equals(PlaceStatus.OPEN)) {
            final ProjectInfo projectInfo = getProjectInfo();
            final PlaceRequest place = placeGainFocusEvent.getPlace();

            if (place instanceof PathPlaceRequest) {
                final PathPlaceRequest pathPlaceRequest = (PathPlaceRequest) place;
                setupLibraryBreadCrumbsForAsset(projectInfo,
                                                pathPlaceRequest.getPath());
                showDocks();
            } else if (isLibraryPlace(place)) {
                hideDocks();
                if (place.getIdentifier().equals(PROJECT_SETTINGS)) {
                    setupLibraryBreadCrumbsForAsset(projectInfo,
                                                    null);
                } else if (projectInfo.getProject() != null) {
                    if (place.getIdentifier().equals(LibraryPlaces.PROJECT_SCREEN)
                            || place.getIdentifier().equals(LibraryPlaces.EMPTY_PROJECT_SCREEN)) {
                        setupLibraryBreadCrumbsForProject(projectInfo);
                    }
                }
            }
        }
    }

    public void hideDocks() {
        if (!docksHidden) {
            docks.hide();
            docksHidden = true;
        }
    }

    public void showDocks() {
        if (docksHidden) {
            if (!docksReady) {
                docks.setup(LibraryPlaces.LIBRARY_PERSPECTIVE,
                            new DefaultPlaceRequest(PROJECT_EXPLORER));
                docksReady = true;
            }
            docks.show();
            docksHidden = false;

            libraryPreferences.load(loadedLibraryPreferences -> {
                                        if (loadedLibraryPreferences.isProjectExplorerExpanded()) {
                                            docks.expandProjectExplorer();
                                        }
                                    },
                                    parameter -> {
                                    });
        }
    }

    private boolean isLibraryPlace(final PlaceRequest place) {
        return LIBRARY_PLACES.contains(place.getIdentifier());
    }

    public void newResourceCreated(@Observes final NewResourceSuccessEvent newResourceSuccessEvent) {
        assetDetailEvent.fire(new AssetDetailEvent(getProjectInfo(),
                                                   newResourceSuccessEvent.getPath()));
    }

    public void assetRenamedAccepted(@Observes final ConcurrentRenameAcceptedEvent concurrentRenameAcceptedEvent) {
        final ProjectInfo projectInfo = getProjectInfo();
        final ObservablePath path = concurrentRenameAcceptedEvent.getPath();
        goToAsset(projectInfo,
                  path);
        setupLibraryBreadCrumbsForAsset(projectInfo,
                                        path);
    }

    public void assetSelected(@Observes final AssetDetailEvent assetDetails) {
        goToAsset(assetDetails.getProjectInfo(),
                  assetDetails.getPath());
    }

    public void setupToolBar() {
        breadcrumbs.clearBreadCrumbsAndToolBars(LibraryPlaces.LIBRARY_PERSPECTIVE);
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  getOURepoLabel(),
                                  () -> goToLibrary());
        breadcrumbs.addToolbar(LibraryPlaces.LIBRARY_PERSPECTIVE,
                               libraryToolbar.getView().getElement());
    }

    public void setupLibraryBreadCrumbs() {
        breadcrumbs.clearBreadCrumbs(LibraryPlaces.LIBRARY_PERSPECTIVE);
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  getOURepoLabel(),
                                  () -> goToLibrary());
    }

    public void setupLibraryBreadCrumbsForNewProject() {
        breadcrumbs.clearBreadCrumbs(LibraryPlaces.LIBRARY_PERSPECTIVE);
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  getOURepoLabel(),
                                  () -> goToLibrary());
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  ts.getTranslation(LibraryConstants.NewProject),
                                  () -> goToNewProject());
    }

    public void setupLibraryBreadCrumbsForProject(final ProjectInfo projectInfo) {
        breadcrumbs.clearBreadCrumbs(LibraryPlaces.LIBRARY_PERSPECTIVE);
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  getOURepoLabel(),
                                  () -> goToLibrary());
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  projectInfo.getProject().getProjectName(),
                                  () -> goToProject(projectInfo));
    }

    public void setupLibraryBreadCrumbsForAsset(final ProjectInfo projectInfo,
                                                final Path path) {
        String assetName;
        if (path != null) {
            assetName = resourceUtils.getBaseFileName(path);
        } else {
            assetName = ts.format(LibraryConstants.LibraryBreadcrumbs_Settings);
        }

        breadcrumbs.clearBreadCrumbs(LibraryPlaces.LIBRARY_PERSPECTIVE);
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  getOURepoLabel(),
                                  () -> goToLibrary());
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  projectInfo.getProject().getProjectName(),
                                  () -> goToProject(projectInfo));
        breadcrumbs.addBreadCrumb(LibraryPlaces.LIBRARY_PERSPECTIVE,
                                  assetName,
                                  () -> goToAsset(projectInfo,
                                                  path));
    }

    public void refresh() {
        libraryToolbar.init(() -> {
            setupToolBar();
            goToLibrary();
            hideDocks();
            examplesUtils.refresh();
        });
    }

    public void goToLibrary() {
        libraryService.call(new RemoteCallback<Boolean>() {
            @Override
            public void callback(final Boolean hasProjects) {
                libraryService.call(libraryInfo -> {
                    final PlaceRequest placeRequest = new ConditionalPlaceRequest(LibraryPlaces.LIBRARY_SCREEN)
                            .when(p -> hasProjects)
                            .orElse(new DefaultPlaceRequest(LibraryPlaces.EMPTY_LIBRARY_SCREEN));
                    final PartDefinitionImpl part = new PartDefinitionImpl(placeRequest);
                    part.setSelectable(false);

                    closeLibraryPlaces();
                    placeManager.goTo(part,
                                      libraryPerspective.getRootPanel());

                    setupLibraryBreadCrumbs();

                    // TODO is this still needed?
                    projectContextChangeEvent.fire(new ProjectContextChangeEvent(getSelectedOrganizationalUnit(),
                                                                                 getSelectedRepository(),
                                                                                 getSelectedBranch()));
                }).getLibraryInfo(getSelectedRepository(),
                                  getSelectedBranch());
            }
        }).hasProjects(getSelectedRepository(),
                       getSelectedBranch());
    }

    public void goToProject(final ProjectInfo projectInfo) {
        libraryService.call(hasAssets -> {
            final PlaceRequest projectScreen = new ConditionalPlaceRequest(LibraryPlaces.PROJECT_SCREEN)
                    .when(p -> (Boolean) hasAssets)
                    .orElse(new DefaultPlaceRequest(LibraryPlaces.EMPTY_PROJECT_SCREEN));
            final PartDefinitionImpl part = new PartDefinitionImpl(projectScreen);
            part.setSelectable(false);

            closeLibraryPlaces();
            placeManager.goTo(part,
                              libraryPerspective.getRootPanel());

            projectDetailEvent.fire(new ProjectDetailEvent(projectInfo));
            projectContextChangeEvent.fire(new ProjectContextChangeEvent(projectInfo.getOrganizationalUnit(),
                                                                         projectInfo.getRepository(),
                                                                         projectInfo.getBranch(),
                                                                         projectInfo.getProject()));

            setupLibraryBreadCrumbsForProject(projectInfo);
        }).hasAssets(projectInfo.getProject());
    }

    public void goToAsset(final ProjectInfo projectInfo,
                          final Path path) {
        final PlaceRequest placeRequest = generatePlaceRequest(path);
        placeManager.goTo(placeRequest);

        if (path != null) {
            final ObservablePath observablePath = ((PathPlaceRequest) placeRequest).getPath();
            observablePath.onRename(() -> setupLibraryBreadCrumbsForAsset(projectInfo,
                                                                          observablePath));
        }
    }

    public void goToNewProject() {
        final DefaultPlaceRequest placeRequest = new DefaultPlaceRequest(LibraryPlaces.NEW_PROJECT_SCREEN);
        final PartDefinitionImpl part = new PartDefinitionImpl(placeRequest);
        part.setSelectable(false);

        closeLibraryPlaces();
        placeManager.goTo(part,
                          libraryPerspective.getRootPanel());
        setupLibraryBreadCrumbsForNewProject();
    }

    public void goToSettings(final ProjectInfo projectInfo) {
        assetDetailEvent.fire(new AssetDetailEvent(projectInfo,
                                                   null));
    }

    public OrganizationalUnit getSelectedOrganizationalUnit() {
        return libraryToolbar.getSelectedOrganizationalUnit();
    }

    public Repository getSelectedRepository() {
        return libraryToolbar.getSelectedRepository();
    }

    public String getSelectedBranch() {
        return libraryToolbar.getSelectedBranch();
    }

    PlaceRequest generatePlaceRequest(final Path path) {
        if (path == null) {
            return new DefaultPlaceRequest(PROJECT_SETTINGS);
        }

        return createPathPlaceRequest(path);
    }

    PathPlaceRequest createPathPlaceRequest(final Path path) {
        return new PathPlaceRequest(path);
    }

    private String getOURepoLabel() {
        return getSelectedOrganizationalUnit().getName() + " (" + getSelectedRepository().getAlias() + ")";
    }

    void closeLibraryPlaces() {
        LIBRARY_PLACES.forEach(place -> placeManager.closePlace(place));
    }
}
