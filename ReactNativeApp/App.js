/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import type {Node} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';
import RNSInfo from 'react-native-sensitive-info';

import UserComponent from './Components/UserComponent';
import TestComponent from './Components/TestComponent';
import {BottomNavigation} from 'react-native-paper';
import CameraCustom from './Components/CameraCustom';
import {Provider as PaperProvider} from 'react-native-paper';

const App: () => Node = () => {
  const TestComponentRoute = () => <TestComponent />;
  const CameraRoute = () => <CameraCustom />;
  const UserRoute = () => <UserComponent />;
  const [index, setIndex] = React.useState(0);
  const [routes] = React.useState([
    {key: 'account', title: 'Account', icon: 'account'},
    {key: 'camera', title: 'Galery', icon: 'image'},
    {key: 'album', title: 'Albums', icon: 'folder-multiple-image'},
  ]);
  const renderScene = BottomNavigation.SceneMap({
    account: UserRoute,
    camera: CameraRoute,
    album: TestComponentRoute,
  });

  //login handler
  let token = RNSInfo.getItem('user_token', {
    sharedPreferencesName: 'mySharedPrefs',
    keychainService: 'myKeychain',
  });
  console.log(token);

  return (
    <PaperProvider>
      <BottomNavigation
        navigationState={{index, routes}}
        onIndexChange={setIndex}
        renderScene={renderScene}
      />
    </PaperProvider>
  );
};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
