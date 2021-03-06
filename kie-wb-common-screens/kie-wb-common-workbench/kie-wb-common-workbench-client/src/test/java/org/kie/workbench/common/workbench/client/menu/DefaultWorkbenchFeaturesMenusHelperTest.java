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

package org.kie.workbench.common.workbench.client.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.workbench.client.admin.DefaultAdminPageHelper;
import org.kie.workbench.common.workbench.client.resources.i18n.DefaultWorkbenchConstants;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.views.pfly.menu.UserMenu;
import org.uberfire.client.workbench.widgets.menu.UtilityMenuBar;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class DefaultWorkbenchFeaturesMenusHelperTest {

    @Mock
    protected SyncBeanManager iocManager;

    @Mock
    private ActivityManager activityManager;

    @Mock
    protected Caller<AuthenticationService> authService;

    @Mock
    protected AuthenticationService authServiceImpl;

    @Mock
    protected User identity;

    @Mock
    protected UserMenu userMenu;

    @Mock
    protected UtilityMenuBar utilityMenuBar;

    @Mock
    protected PerspectiveManager perspectiveManager;

    @Mock
    protected DefaultAdminPageHelper adminPageHelper;

    @Spy
    @InjectMocks
    private DefaultWorkbenchFeaturesMenusHelper menusHelper;

    @Before
    public void setup() {
        mockConstants();
        mockDefaultPerspective();
        mockRoles();
        mockGroups();
        mockIocManager();
    }

    @Test
    public void getHomeViewsWithoutSocial() {
        final boolean socialEnabled = false;

        List<? extends MenuItem> homeMenuItems = menusHelper.getHomeViews( socialEnabled );

        assertEquals( 2, homeMenuItems.size() );
        assertEquals( menusHelper.constants.HomePage(), homeMenuItems.get( 0 ).getCaption() );
        assertEquals( menusHelper.constants.Admin(), homeMenuItems.get( 1 ).getCaption() );
    }

    @Test
    public void getHomeViewsWithSocial() {
        final boolean socialEnabled = true;

        List<? extends MenuItem> homeMenuItems = menusHelper.getHomeViews( socialEnabled );

        assertEquals( 4, homeMenuItems.size() );
        assertEquals( menusHelper.constants.HomePage(), homeMenuItems.get( 0 ).getCaption() );
        assertEquals( menusHelper.constants.Admin(), homeMenuItems.get( 1 ).getCaption() );
        assertEquals( menusHelper.constants.Timeline(), homeMenuItems.get( 2 ).getCaption() );
        assertEquals( menusHelper.constants.People(), homeMenuItems.get( 3 ).getCaption() );
    }

    @Test
    public void getAuthoringViewsTest() {
        List<? extends MenuItem> authoringMenuItems = menusHelper.getAuthoringViews();

        assertEquals( 5, authoringMenuItems.size() );
        assertEquals( menusHelper.constants.ProjectAuthoring(), authoringMenuItems.get( 0 ).getCaption() );
        assertEquals( menusHelper.constants.Contributors(), authoringMenuItems.get( 1 ).getCaption() );
        assertEquals( menusHelper.constants.ArtifactRepository(), authoringMenuItems.get( 2 ).getCaption() );
        assertEquals( menusHelper.constants.Administration(), authoringMenuItems.get( 3 ).getCaption() );
        assertEquals( menusHelper.constants.Library(), authoringMenuItems.get( 4 ).getCaption() );
    }

    @Test
    public void getProcessManagementViewsTest() {
        List<? extends MenuItem> processManagementMenuItems = menusHelper.getProcessManagementViews();

        assertEquals( 2, processManagementMenuItems.size() );
        assertEquals( menusHelper.constants.ProcessDefinitions(), processManagementMenuItems.get( 0 ).getCaption() );
        assertEquals( menusHelper.constants.ProcessInstances(), processManagementMenuItems.get( 1 ).getCaption() );
    }

    @Test
    public void getExtensionsViewsTest() {
        List<? extends MenuItem> extensionsMenuItems = menusHelper.getExtensionsViews();

        assertEquals( 4, extensionsMenuItems.size() );
        assertEquals( menusHelper.constants.Plugins(), extensionsMenuItems.get( 0 ).getCaption() );
        assertEquals( menusHelper.constants.Apps(), extensionsMenuItems.get( 1 ).getCaption() );
        assertEquals( menusHelper.constants.DataSets(), extensionsMenuItems.get( 2 ).getCaption() );
        assertEquals( menusHelper.constants.DataSources(), extensionsMenuItems.get( 3 ).getCaption() );
    }

    @Test
    public void addRolesMenuItemsTest() {
        menusHelper.addRolesMenuItems();

        ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass( Menus.class );
        verify( userMenu, times( 3 ) ).addMenus( menusCaptor.capture() );

        List<Menus> menusList = menusCaptor.getAllValues();

        assertEquals( 3, menusList.size() );

        assertEquals( 1, menusList.get( 0 ).getItems().size() );
        assertEquals( 1, menusList.get( 1 ).getItems().size() );
        assertEquals( 1, menusList.get( 2 ).getItems().size() );

        checkIfMenuContainsRole( menusList, menusHelper.constants.LogOut() );
        checkIfMenuContainsRole( menusList, "Role: role1" );
        checkIfMenuContainsRole( menusList, "Role: role2" );
    }

    @Test
    public void addGroupsMenuItemsTest() {
        menusHelper.addGroupsMenuItems();

        ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass( Menus.class );
        verify( userMenu, times( 2 ) ).addMenus( menusCaptor.capture() );

        List<Menus> menusList = menusCaptor.getAllValues();

        assertEquals( 2, menusList.size() );

        assertEquals( 1, menusList.get( 0 ).getItems().size() );
        assertEquals( 1, menusList.get( 1 ).getItems().size() );

        checkIfMenuContainsRole( menusList, "Group: group1" );
        checkIfMenuContainsRole( menusList, "Group: group2" );
    }

    private void checkIfMenuContainsRole( final List<Menus> menusList,
                                          String role ) {
        assertContains( menusList,
                        ( menus ) -> contains( ( (Menus) menus ).getItems(),
                                               ( menuItem ) -> ( (MenuItem) menuItem )
                                                       .getCaption().equals( role )
                                             )
                      );
    }

    @Test
    public void addWorkbenchViewModeSwitcherMenuItemTest() {
        menusHelper.addWorkbenchViewModeSwitcherMenuItem();

        ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass( Menus.class );
        verify( userMenu, times( 1 ) ).addMenus( menusCaptor.capture() );

        List<Menus> menusList = menusCaptor.getAllValues();

        assertEquals( 1, menusList.size() );
        assertEquals( 1, menusList.get( 0 ).getItems().size() );
    }

    @Test
    public void addWorkbenchConfigurationMenuItemTest() {
        menusHelper.addWorkbenchConfigurationMenuItem();

        ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass( Menus.class );
        verify( utilityMenuBar, times( 1 ) ).addMenus( menusCaptor.capture() );

        List<Menus> menusList = menusCaptor.getAllValues();

        assertEquals( 1, menusList.size() );
        assertEquals( 1, menusList.get( 0 ).getItems().size() );
    }

    @Test
    public void addUtilitiesMenuItemsTest() {
        menusHelper.addUtilitiesMenuItems();

        ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass( Menus.class );
        verify( utilityMenuBar, times( 1 ) ).addMenus( menusCaptor.capture() );

        List<Menus> menusList = menusCaptor.getAllValues();

        assertEquals( 1, menusList.size() );
        assertEquals( 5, menusList.get( 0 ).getItems().size() );
    }

    @Test
    public void addLogoutMenuItemTest() {
        menusHelper.addLogoutMenuItem();

        ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass( Menus.class );
        verify( userMenu, times( 1 ) ).addMenus( menusCaptor.capture() );

        List<Menus> menusList = menusCaptor.getAllValues();

        assertEquals( 1, menusList.size() );

        assertEquals( 1, menusList.get( 0 ).getItems().size() );

        checkIfMenuContainsRole( menusList, menusHelper.constants.LogOut() );
    }

    @Test
    public void logoutCommandTest() {
        final DefaultWorkbenchFeaturesMenusHelper.LogoutCommand logoutCommand = spy( menusHelper.new LogoutCommand() );

        logoutCommand.execute();

        verify( perspectiveManager ).savePerspectiveState( any( Command.class ) );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void logoutCommandRedirectIncludesLocaleTest() throws Throwable {
        final DefaultWorkbenchFeaturesMenusHelper.LogoutCommand logoutCommand = spy( menusHelper.new LogoutCommand() {

            @Override
            void doRedirect( final String url ) {
                //Do nothing
            }

            @Override
            String getGWTModuleBaseURL() {
                return "/gwtModule/";
            }

            @Override
            String getGWTModuleName() {
                return "gwtModule";
            }

            @Override
            String getLocale() {
                return "en_GB";
            }
        } );

        logoutCommand.execute();

        final ArgumentCaptor<Command> postSaveStateCommandCaptor = ArgumentCaptor.forClass( Command.class );
        final ArgumentCaptor<String> redirectURLCaptor = ArgumentCaptor.forClass( String.class );

        when( authService.call( any( RemoteCallback.class ) ) ).thenAnswer( ( final InvocationOnMock invocation ) -> {
            ( (RemoteCallback) invocation.getArguments()[ 0 ] ).callback( null );
            return authServiceImpl;
        } );
        verify( perspectiveManager ).savePerspectiveState( postSaveStateCommandCaptor.capture() );

        final Command postSaveStateCommand = postSaveStateCommandCaptor.getValue();
        postSaveStateCommand.execute();

        verify( logoutCommand ).getRedirectURL();
        verify( logoutCommand ).doRedirect( redirectURLCaptor.capture() );
        verify( authServiceImpl ).logout();

        final String redirectURL = redirectURLCaptor.getValue();
        assertTrue( redirectURL.contains( "/logout.jsp?locale=en_GB" ) );
    }

    private void mockGroups() {
        Set<Group> groups = new HashSet<>( 2 );
        groups.add( () -> "group1" );
        groups.add( () -> "group2" );

        doReturn( groups ).when( identity ).getGroups();
    }

    private void mockRoles() {
        Set<Role> roles = new HashSet<>( 2 );
        roles.add( () -> "role1" );
        roles.add( () -> "role2" );

        doReturn( roles ).when( identity ).getRoles();
    }

    private void mockDefaultPerspective() {
        AbstractWorkbenchPerspectiveActivity defaultPerspective = mock( AbstractWorkbenchPerspectiveActivity.class );
        when( defaultPerspective.getIdentifier() ).thenReturn( "defaultPerspective" );
        doReturn( defaultPerspective ).when( menusHelper ).getDefaultPerspectiveActivity();
    }

    private void mockConstants() {
        menusHelper.constants = mock( DefaultWorkbenchConstants.class, (Answer) invocation -> {
            if ( String.class.equals( invocation.getMethod().getReturnType() ) ) {
                return invocation.getMethod().getName();
            } else {
                return RETURNS_DEFAULTS.answer( invocation );
            }
        } );
    }

    private void mockIocManager() {
        doAnswer( invocationOnMock -> createSyncBeanDef( (Class<?>) invocationOnMock.getArguments()[ 0 ] ) )
                .when( iocManager ).lookupBean( any( Class.class ) );
    }

    private <T> SyncBeanDef<T> createSyncBeanDef( Class<T> clazz ) {
        final SyncBeanDef syncBeanDef = mock( SyncBeanDef.class );
        doReturn( mock( clazz ) ).when( syncBeanDef ).getInstance();
        doReturn( mock( clazz ) ).when( syncBeanDef ).newInstance();

        return syncBeanDef;
    }

    private <T> boolean assertContains( final List<T> objects,
                                        final Checker checker ) {
        return assertContains( objects, checker, 1 );
    }

    private <T> boolean assertContains( final List<T> objects,
                                        final Checker<T> checker,
                                        int count ) {
        boolean result = contains( objects, checker, count );

        if ( !result ) {
            fail( "The passed list does not contain " + count + " element(s) that matches the passed condition." );
        }

        return result;
    }

    private <T> boolean contains( final List<T> objects,
                                  final Checker checker ) {
        return contains( objects, checker, 1 );
    }

    private <T> boolean contains( final List<T> objects,
                                  final Checker<T> checker,
                                  int count ) {
        for ( T object : objects ) {
            if ( checker.check( object ) ) {
                if ( --count == 0 ) {
                    return true;
                }
            }
        }

        return false;
    }

    private interface Checker<T> {

        boolean check( T object );
    }
}
