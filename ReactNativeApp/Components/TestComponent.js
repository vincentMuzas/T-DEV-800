import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
  Keyboard,
  TouchableOpacity,
} from 'react-native';
import axios from 'axios';

import SInfo from 'react-native-sensitive-info';
import {Colors} from 'react-native/Libraries/NewAppScreen';
import {
  Appbar,
  TextInput,
  Card,
  Avatar,
  Menu, 
  Button,
  Divider
} from 'react-native-paper';
import FastImage from 'react-native-fast-image';

import {placeholder, server_ip} from '../src/consts';
import {black} from 'react-native-paper/lib/typescript/styles/colors';

export default class TestComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      albums: [],
      allPictures: [],
      title: 'Albums',
      selectedAlbum: null,
      token: null,
      newAlbumInput: null,
      fieldOpened: false,
      addPicture: false,
      menuVisible: false,
      deleteMode: false,
      searchName: '',
    };
    this.getToken();
  }

  getToken() {
    async function getTokenData() {
      return await SInfo.getItem('user_token', {});
    }
    getTokenData().then(x => {
      this.setState({token: x});
      this.fetchAlbums();
    });
  }

  newAlbum = () => {
    Keyboard.dismiss();
    fetch(`http://${server_ip}:8080/album`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${this.state.token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: this.state.newAlbumInput,
      }),
    })
      .then(res => {
        this.setState({newAlbumInput: null});
        this.fetchAlbums();
      })
      .catch(e => console.log(e))
      .done();
  };

  fetchAlbums() {
    fetch(`http://${server_ip}:8080/albums`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${this.state.token}`,
      },
    })
      .then(res => {
        if (res.ok) {
          return res.json();
        }
        throw `${res.status}: Network failure`;
      })
      .then(json => {
        json.map((album, index) => {
          album.pictures.map(pict => {
            pict.uri = `http://${server_ip}:4566/dev-bucket/${pict.file.fullPath}`;
          });
        });
        this.setState({albums: json});
      })
      .catch(e => console.log(e))
      .done();
  }

  _goBack = () =>
    this.setState({selectedAlbum: null, addPicture: false, menuVisible: false});

  fetchAllPictures = () => {
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
        let arr = [];
        json.map(image => {
          image.uri = `http://${server_ip}:4566/dev-bucket/${image.file.fullPath}`;
          arr.unshift(image);
        });
        this.setState({allPictures: arr});
      })
      .catch(e => console.log(e))
      .done();
  };

  addPictureSend(index) {
    fetch(
      `http://${server_ip}:8080/album/${
        this.state.albums[this.state.selectedAlbum].id
      }/${this.state.allPictures[index].id}`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${this.state.token}`,
        },
      },
    )
      .then(res => {
        if (res.ok) {
          return res;
        }
        throw 'Network error';
      })
      .then(json => {
        let truc = this.state.albums;
        truc[this.state.selectedAlbum].pictures.unshift(
          this.state.allPictures[index],
        );
        this.setState({addPicture: false, menuVisible: false, albums: truc});
      })
      .catch(e => console.log(e))
      .done();
  }

  deleteAlbum() {
    let album_id = this.state.albums[this.state.selectedAlbum].id;
    fetch(`http://${server_ip}:8080/album/${album_id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${this.state.token}`,
      },
    })
      .then(res => {
        if (res.ok) {
          this._goBack();
          this.fetchAlbums();
          return res;
        }
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  }

  deleteImageFromAlbum(id) {
    let album_id = this.state.albums[this.state.selectedAlbum].id;
    fetch(`http://${server_ip}:8080/album/${album_id}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${this.state.token}`,
      },
    })
      .then(res => {
        if (res.ok) {
          let albums = this.state.albums;
          albums[this.state.selectedAlbum].pictures.forEach((element, idx) => {
            if (element.id === id) {
              albums[this.state.selectedAlbum].pictures.splice(idx, 1);
            }
          });
          this.setState({albums: albums});
          return res;
        }
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  }

  shareSend = () => {
    Keyboard.dismiss();
    fetch(
      `http://${server_ip}:8080/album/share/${
        this.state.albums[this.state.selectedAlbum].id
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
          let albums = this.state.albums;
          albums[this.state.selectedAlbum].user_shared.unshift({username: this.state.shareName});
          this.setState({shareName: null, shareResponse: null, albums: albums});
          return res;
        }
        this.setState({shareResponse: 'ERROR: User not found'});
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  };

  deleteShare = (username, idx) => {
    Keyboard.dismiss();
    fetch(
      `http://${server_ip}:8080/album/share/${
        this.state.albums[this.state.selectedAlbum].id
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
          let albums = this.state.albums;
          albums[this.state.selectedAlbum].user_shared.splice(idx, 1);
          this.setState({shareResponse: null, albums: albums});
          return res;
        }
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  };

  imageStyleIfShared = (image) => {
    if (image.myAlbum) {
      return {};
    }
    return {
      borderColor: "#1565c0",
      borderWidth: 5,
    }
  }

  tagSend = () => {
    Keyboard.dismiss();
    fetch(
      `http://${server_ip}:8080/album/${this.state.albums[this.state.selectedAlbum].id}/tag`,
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
          let all_album = this.state.albums;
          all_album[this.state.selectedAlbum].tags.unshift(this.state.tagName);
          this.setState({tagName: null, shareResponse: null, albums: all_album});
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
      `http://${server_ip}:8080/album/${this.state.albums[this.state.selectedAlbum].id}/tag`,
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
          let all_album = this.state.albums;
          all_album[this.state.selectedAlbum].tags.splice(idx, 1);
          this.setState({tagName: null, shareResponse: null, albums: all_album});
          return res;
        }
        this.setState({tagResponse: 'ERROR: unable to delete tag'});
        throw 'Network error';
      })
      .catch(e => console.log(e))
      .done();
  };

  render() {
    if (this.state.selectedAlbum != null) {
      if (this.state.addPicture) {
        return (
          <View>
            <Appbar.Header>
              <Appbar.BackAction onPress={() => {this.setState({addPicture: false, menuVisible: false})}} />
              <Appbar.Content
                title={`Add picture: ${
                  this.state.albums[this.state.selectedAlbum].name
                }`}
              />
            </Appbar.Header>
            <ScrollView style={{marginBottom: 56}}>
              <View style={styles.ImageSections}>
                {this.state.allPictures.map((d, index) => {
                  return d.myPicture ? (  
                    <TouchableOpacity
                    style={styles.imagesSpacer}
                    onPress={() => {
                      this.addPictureSend(index);
                    }}
                    key={index}>
                    <FastImage source={{uri: d.uri}} style={[styles.images]} />
                    </TouchableOpacity>
                  ) : null
                })}
              </View>
            </ScrollView>
          </View>
        );
      } else {
        return (
          <View>
            <Appbar.Header>
              <Appbar.BackAction onPress={this._goBack} />
              <Appbar.Content
                title={`Album: ${
                  this.state.albums[this.state.selectedAlbum].name
                }`}
              />
              {this.state.albums[this.state.selectedAlbum].myAlbum ? (
              <><Appbar.Action
                  icon={this.state.editOpen ? "pencil-off" : "pencil"}
                  color='white'
                  onPress={() => this.setState({ editOpen: !this.state.editOpen })} /><Menu
                    visible={this.state.menuVisible}
                    onDismiss={() => {
                      this.setState({ menuVisible: false });
                    } }
                    anchor={<Appbar.Action
                      icon="menu"
                      color="white"
                      onPress={() => {
                        this.setState({ menuVisible: true });
                      } } />}>
                    <Menu.Item
                      icon="image-plus"
                      disabled={this.state.albums[this.state.selectedAlbum].myAlbum ===
                        false}
                      onPress={() => {
                        this.setState({ addPicture: true });
                        this.fetchAllPictures();
                      } }
                      title="Add a picture" />
                    <Menu.Item
                      icon="delete"
                      disabled={this.state.albums[this.state.selectedAlbum].myAlbum ===
                        false}
                      onPress={() => {
                        this.deleteAlbum();
                      } }
                      title="Delete this album" />
                  </Menu></>
              ) : null}
            </Appbar.Header>
            <ScrollView style={{marginBottom: 56}}>
              {this.state.albums[this.state.selectedAlbum].myAlbum && this.state.editOpen ? (
                <Card>
                  <Card.Title title="Share options" subtitle={this.state.shareResponse} subtitleStyle={{color: 'red'}} />
                  <TextInput
                    label="Share With"
                    onChangeText={text => this.setState({ shareName: text })}
                    value={this.state.shareName}
                    right={<TextInput.Icon name="share" onPress={this.shareSend} />} />
                    {this.state.albums[this.state.selectedAlbum].user_shared.map((elem, idx) =>
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
                    label="Add a Tag on the album"
                    onChangeText={text => this.setState({ tagName: text })}
                    value={this.state.tagName}
                    right={<TextInput.Icon name="plus" onPress={this.tagSend} />} />
                  {this.state.albums[this.state.selectedAlbum].tags.map((elem, idx) =>
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
              <View style={styles.ImageSections}>
                {this.state.albums[this.state.selectedAlbum].pictures.map(
                  (d, index) => (
                    <TouchableOpacity
                      style={styles.imagesSpacer}
                      onPress={() => {
                        console.log(d);
                      }}
                      onLongPress={() => {
                        this.deleteImageFromAlbum(d.id);
                      }}
                      key={index}>
                      <FastImage source={{uri: d.uri}} style={styles.images} />
                    </TouchableOpacity>
                  ),
                )}
              </View>
            </ScrollView>
          </View>
        );
      }
    }
    return (
      <View>
        <Appbar.Header>
          <Appbar.Content title="My Albums" />
          <TextInput
            label="Search"
            style={{left: 0, right: 0, flex: 1, height: 56}}
            onChangeText={text => this.setState({ searchName: text })}
            value={this.state.searchName}
            right={<TextInput.Icon name="magnify" />}
          />
          <Appbar.Action
            icon={this.state.fieldOpened ? "minus" : "plus"}
            onPress={() => this.setState({fieldOpened: !this.state.fieldOpened})} />
        </Appbar.Header>
        {this.state.fieldOpened? (
          <TextInput
            label="Album Name"
            onChangeText={text => this.setState({newAlbumInput: text})}
            value={this.state.newAlbumInput}
            right={<TextInput.Icon name="send" onPress={this.newAlbum} />}
          />
        ) : null}
        <SafeAreaView>
          <ScrollView style={{marginBottom: 56}}>
            {this.state.albums.map((elem, index) => {
              var obj = (
                elem.pictures.length ? (
                  <TouchableOpacity
                    key={index}
                    onPress={() => this.setState({selectedAlbum: index})}>
                    <Card.Title
                      title={elem.name}
                      subtitle={'Owner: ' + elem.user.username}
                      left={props => (
                        <FastImage
                          source={{uri: elem.pictures[0].uri}}
                          style={[styles.images_icons, this.imageStyleIfShared(elem)]}
                        />
                      )}
                    />
                  </TouchableOpacity>
                  ) : (
                    <TouchableOpacity
                      key={index}
                      onPress={() => this.setState({selectedAlbum: index})}>
                      <Card.Title
                        title={elem.name}
                        subtitle={'Owner: ' + elem.user.username}
                        left={props => (
                          <Avatar.Image size={48} source={placeholder} />
                        )}
                      />
                    </TouchableOpacity>
                  )
                )
                if (this.state.searchName.length === 0) {
                  return obj;
                }
                for (const idx in elem.tags) {
                  if (elem.tags[idx].includes(this.state.searchName))
                    return obj;
                }
                return null;
              }
            )}
          </ScrollView>
        </SafeAreaView>
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
  images_icons: {
    width: 48,
    height: 48,
    resizeMode: FastImage.resizeMode.contain,
    backgroundColor: 'grey',
  },
});
