/* eslint-disable react-native/no-inline-styles */
import React, {Fragment, Component} from 'react';
import {launchCamera, launchImageLibrary} from 'react-native-image-picker';
import {
  Keyboard,
  StyleSheet,
  ScrollView,
  View,
  Dimensions,
  TouchableOpacity,
} from 'react-native';
import SInfo from 'react-native-sensitive-info';
import {Colors} from 'react-native/Libraries/NewAppScreen';
import FastImage from 'react-native-fast-image';

import {server_ip, loading_gif} from '../src/consts';
import { Appbar, Menu, TextInput, Card, Button, Text, Divider } from 'react-native-paper';

export default class CameraCustom extends Component {
  constructor(props) {
    super(props);
    this.state = {
      token: null,
      selected_files: [] /* TODO: need to get the images from the API here */,
      selected_image: null,
      selected_image_idx: null,
      shareName: null,
      searchName: '',
      editOpen: false,
      nb_uploading: 0,
    };
    this.getToken();
  }

  getToken() {
    async function getTokenData() {
      return await SInfo.getItem('user_token', {});
    }
    getTokenData().then(x => {
      this.setState({token: x});
      this.getPhotosFromServer();
    });
  }

  getPhotosFromServer = () => {
    fetch(`http://${server_ip}:8080/pictures`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${this.state.token}`,
      },
    })
      .then(res => {
        if (!res.ok) {
          throw `${res.status}: Network failure`;
        }
        return res.json();
      })
      .then(json => {
        json.map(image => {
          image.uri = `http://${server_ip}:4566/dev-bucket/${image.file.fullPath}`;
        });
        this.setState({selected_files: json.reverse(), menuVisible: false});
      })
      .catch(e => console.log(e))
      .done();
  };

  zoomImage(index) {
    let obj = this.state.selected_files[index];
    obj.index = index;
    this.setState({
      selected_image: obj,
      selected_image_idx: index,
    });
  }
  dezoomImage = () => {
    this.setState({
      selected_image: null,
    });
  };

  deleteSelectedImage = () => {
    fetch(`http://${server_ip}:8080/picture/${this.state.selected_image.id}`, {
      method: 'DELETE',
      headers: {
        Authorization: 'Bearer ' + this.state.token,
      },
    })
      .then(res => {
        if (!res.ok) {
          throw `${res.status}: Network failure`;
        }
        let updated = this.state.selected_files;
        updated.splice(this.state.selected_image.index, 1);
        this.setState({selected_image: null, selected_files: updated});
      })
      .catch(e => console.log(e))
      .done();
  };

  launchImageLibrary = () => {
    this.setState({ menuVisible: false })
    let options = {
      //includeBase64: true,
      mediaType: 'images',
      selectionLimit: 0,
    };
    launchImageLibrary(options, response => {
      if (response.didCancel) {
      } else if (response.error) {
      } else if (response.customButton) {
      } else {
        this.setState({nb_uploading: this.state.nb_uploading + response.assets.length});
        response.assets.map(image => {
          //console.log(image.base64);
          this.uploadImages(image);
        });
      }
    });
  };

  launchCamera = () => {
    this.setState({ menuVisible: false })
    let options = {
      //includeBase64: true,
      mediaType: 'photo',
      cameraType: 'back',
      saveToPhotos: false,
    };
    launchCamera(options, response => {
      if (response.didCancel) {
      } else if (response.error) {
      } else if (response.customButton) {
      } else {
        this.setState({nb_uploading: this.state.nb_uploading + 1});
        response.assets.map(image => {
          this.uploadImages(image);
        });
      }
    });
  };

  uploadImages(image) {
    let body = new FormData();
    body.append('file', {
      uri: image.uri,
      name: image.fileName,
      type: image.type,
    });
    body.append('Content-Type', image.type);
    fetch(`http://${server_ip}:8080/picture`, {
      method: 'POST',
      headers: {
        Authorization: 'Bearer ' + this.state.token,
        'Content-Type': 'multipart/form-data',
        otherHeader: 'foo',
      },
      body: body,
    })
      .then(res => {
        if (!res.ok) {
          this.setState({nb_uploading: this.state.nb_uploading - 1});
          throw `${res.status}: Network failure`;
        }
        return res.json();
      })
      .then(json => {
        let loc_file = this.state.selected_files;
        image.id = json.id;
        image.user_shared = [];
        image.file = {user: {username: ''}}
        image.myPicture = true;
        loc_file.unshift(image);
        this.setState({selected_files: loc_file, nb_uploading: this.state.nb_uploading - 1});
      })
      .catch(e => console.log(e))
      .done();
  }

  shareSend = () => {
    Keyboard.dismiss();
    fetch(
      `http://${server_ip}:8080/picture/share/${
        this.state.selected_image.id
      }`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${this.state.token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({name: this.state.shareName}),
      },
    )
      .then(res => {
        if (res.ok) {
          let all_album = this.state.selected_files;
          all_album[this.state.selected_image_idx].user_shared.unshift({username: this.state.shareName});
          this.setState({shareName: null, shareResponse: null, selected_files: all_album});
          return res;
        }
        this.setState({shareResponse: 'ERROR: User not found'});
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  };

  deleteShare = (username, idx) => {
    fetch(
      `http://${server_ip}:8080/picture/share/${
        this.state.selected_image.id
      }`,
      {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${this.state.token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({name: username}),
      },
    )
      .then(res => {
        if (res.ok) {
          let all_album = this.state.selected_files;
          all_album[this.state.selected_image_idx].user_shared.splice(idx, 1);
          this.setState({shareName: null, shareResponse: null, selected_files: all_album});
          return res;
        }
        this.setState({shareResponse: 'ERROR: unable to delete share'});
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  };

  tagSend = () => {
    Keyboard.dismiss();
    fetch(
      `http://${server_ip}:8080/picture/${this.state.selected_image.id}/tag`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${this.state.token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({tag: this.state.tagName}),
      },
    )
      .then(res => {
        if (res.ok) {
          let all_album = this.state.selected_files;
          all_album[this.state.selected_image_idx].tags.unshift(this.state.tagName);
          this.setState({tagName: null, shareResponse: null, selected_files: all_album});
          return res;
        }
        this.setState({tagResponse: 'ERROR: unable to set tag'});
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  }

  deleteTag = (name, idx) => {
    fetch(
      `http://${server_ip}:8080/picture/${this.state.selected_image.id}/tag`,
      {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${this.state.token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({tag: name}),
      },
    )
      .then(res => {
        if (res.ok) {
          let all_album = this.state.selected_files;
          all_album[this.state.selected_image_idx].tags.splice(idx, 1);
          this.setState({shareName: null, shareResponse: null, selected_files: all_album});
          return res;
        }
        this.setState({tagResponse: 'ERROR: unable to delete tag'});
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  };

  imageStyleIfShared = (image) => {
    if (image.myPicture) {
      return {};
    }
    return {
      borderColor: "#1565c0",
      borderWidth: 5,
    }
  }

  render() {
    if (this.state.selected_image) {
      let ratio =
        this.state.selected_image.height / this.state.selected_image.width;
      let calc_height = Dimensions.get('screen').width * ratio;
      if (!calc_height) {
        calc_height = 0;
      }
      return (
        <View>
          <Appbar.Header>
            <Appbar.BackAction onPress={() => {this.setState({selected_image: null, editOpen: false})}} />
            <Appbar.Content title={`Image details`} />
            {this.state.selected_image.myPicture ? (
              <>
                <Appbar.Action
                  icon={this.state.editOpen ? "pencil-off": "pencil"}
                  color='white'
                  onPress={() => this.setState({editOpen: !this.state.editOpen})}
                />
                <Menu
                  visible={this.state.menuVisible}
                  onDismiss={() => this.setState({ menuVisible: false })}
                  anchor={<Appbar.Action
                    icon="delete"
                    color="white"
                    onPress={() => this.setState({ menuVisible: true })} />}
                >
                  <Menu.Item
                    icon="alert"
                    title="Confirm Delete"
                    onPress={this.deleteSelectedImage} />
                </Menu>
              </>
            ) : null }
          </Appbar.Header>
          <ScrollView style={{marginBottom: 56}}>
            {this.state.selected_image.myPicture && this.state.editOpen ? (
            <Card>
              <Card.Title title="Share options" subtitle={this.state.shareResponse} subtitleStyle={{color: 'red'}} />
              <TextInput
                label="Share With"
                onChangeText={text => this.setState({ shareName: text })}
                value={this.state.shareName}
                right={<TextInput.Icon name="share" onPress={this.shareSend} />} />
              {this.state.selected_image.user_shared.map((elem, idx) =>
                <View key={idx}>
                  <Card.Title
                    title={elem.username}
                    titleStyle={{fontSize: 16}}
                    right={props => (
                      <Button style={{backgroundColor: "red"}} compact mode="contained" icon="delete" onPress={() => this.deleteShare(elem.username, idx)} />
                      )}
                      rightStyle={{marginRight: 10}}
                      />
                </View>
              )}
              <Divider style={{marginBottom: 10}} />
              <Card.Title title="Tag options" subtitle={this.state.tagResponse} subtitleStyle={{color: 'red'}} />
              <TextInput
                label="Add a Tag on the image"
                onChangeText={text => this.setState({ tagName: text })}
                value={this.state.tagName}
                right={<TextInput.Icon name="plus" onPress={this.tagSend} />} />
              {this.state.selected_image.tags.map((elem, idx) =>
                <View key={idx}>
                  <Card.Title
                    title={elem}
                    titleStyle={{fontSize: 16}}
                    right={props => (
                      <Button style={{backgroundColor: "grey"}} compact mode="contained" icon="delete" onPress={() => this.deleteTag(elem, idx)} />
                      )}
                      rightStyle={{marginRight: 10}}
                      />
                </View>
              )}
              <Divider style={{marginBottom: 10}} />
            </Card>
            ) : null}
            <View style={{width: '100%', height: calc_height}}>
              <FastImage
                source={{uri: this.state.selected_image.uri}}
                style={styles.images}
                onLoad={evt => {
                  let elem = this.state.selected_image;
                  elem.width = evt.nativeEvent.width;
                  elem.height = evt.nativeEvent.height;
                  this.setState({selected_image: elem});
                }}
              />
            </View>
            {this.state.selected_image.myPicture ? null : (
              <Text>{`Shared by ${this.state.selected_image.file.user.username} `}</Text>
            )}
          </ScrollView>
          </View>
          );
    }
    let uploading = [];
    for (let i = 0; i < this.state.nb_uploading; i++) {
      uploading.push(
        <TouchableOpacity
        style={styles.imagesSpacer}
        key={`u${i}`}>
          <FastImage source={loading_gif} style={styles.images} />
        </TouchableOpacity>
      )
    }
    return (
      <View>
        <Appbar.Header>
          <Appbar.Content
            title="My Galery">
          </Appbar.Content>
          <TextInput
            label="Search"
            style={{left: 0, right: 0, flex: 1}}
            onChangeText={text => this.setState({ searchName: text })}
            value={this.state.searchName}
            right={<TextInput.Icon name="magnify" />}
          />
          <Menu
            visible={this.state.menuVisible}
            onDismiss={() => this.setState({ menuVisible: false })}
            anchor={<Appbar.Action
              icon="menu"
              color="white"
              onPress={() => this.setState({ menuVisible: true })} />}
          >
            <Menu.Item
              icon="refresh"
              title="Refresh Galery"
              onPress={this.getPhotosFromServer}
            />
            <Menu.Item
              icon="camera-plus"
              title="Launch Camera"
              onPress={this.launchCamera}
            />
            <Menu.Item
              icon="image-plus"
              title="Upload existing Image"
              onPress={this.launchImageLibrary}
            />
          </Menu>
        </Appbar.Header>
        <ScrollView style={{marginBottom: 56}}>
          <View style={styles.ImageSections}>
            {uploading}
            {this.state.selected_files.map((d, index) =>
              {
                let obj = (
                  <TouchableOpacity
                    style={styles.imagesSpacer}
                    onPress={() => this.zoomImage(index)}
                    key={index}>
                    <FastImage source={{uri: d.uri}} style={[styles.images, this.imageStyleIfShared(d)]} />
                  </TouchableOpacity>
                )
                if (this.state.searchName.length === 0) {
                  return obj;
                }
                for (const idx in d.tags) {
                  if (d.tags[idx].includes(this.state.searchName))
                    return obj;
                }
                return null;
              })}
          </View>
        </ScrollView>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  ImageSections: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  imagesSpacer: {
    width: '33%',
    height: 150,
    padding: 2,
  },
  images: {
    width: '100%',
    height: '100%',
    resizeMode: FastImage.resizeMode.contain,
    backgroundColor: 'grey',
    borderRadius: 3,
    paddingHorizontal: 3,
  },
});
