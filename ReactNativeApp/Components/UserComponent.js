/* eslint-disable react-native/no-inline-styles */
import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
  Dimensions,
} from 'react-native';
import {Colors, ReloadInstructions} from 'react-native/Libraries/NewAppScreen';
import {TextInput, Button, Card, Paragraph} from 'react-native-paper';
import SInfo from 'react-native-sensitive-info';

import {server_ip} from '../src/consts';

export default class UserComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      token: this.getToken(),
      loged_name: null,
      login: null,
      password: null,
      password_repeat: null,
      need_register: false,
      opt_message: null,
      email: null,
    };
  }
  setToken(newToken) {
    async function tokenData(token) {
      await SInfo.setItem('user_token', token, {});
    }
    tokenData(newToken).then(this.getToken());
  }
  getToken() {
    async function getTokenData() {
      return await SInfo.getItem('user_token', {});
    }
    getTokenData().then(x => {
      fetch(`http://${server_ip}:8080/islogged`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${x}`,
        },
      })
        .then(result => {
          if (result.ok) {
            return result.json();
          } else {
            this.logout();
            throw "token expired";
          }
        })
        .then(json => {
          this.setState({token: x, loged_name: json.username});
        })
        .catch(e => console.log(e))
        .done();
    });
  }
  componentDidMount() {}
  login = () => {
    fetch(`http://${server_ip}:8080/login`, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        login: this.state.login,
        password: this.state.password,
      }),
    })
      .then(res => {
        if (!res.ok) {
          throw `${res.status}: Network failure`;
        }
        return res.json();
      })
      .then(json => {
        this.setState({opt_message: json.message});
        if (json.message !== 'Login incorect') {
          this.setToken(json.token);
        }
      })
      .catch(e => console.log(e))
      .done();
  };
  logout = () => {
    SInfo.deleteItem('user_token', {});
    this.setState({token: null, login: null, password: null});
  };
  change_register = () => {
    this.setState({need_register: !this.state.need_register, opt_message: null});
  };
  register = () => {
    if (!this.state.login || this.state.login.length <= 5) {
      this.setState({
        opt_message: 'login not long enough (need >5 characters)',
      });
    } else if (!this.state.password || this.state.password.length <= 5) {
      this.setState({
        opt_message: 'password not long enough (need >5 characters)',
      });
    } else if (this.state.password !== this.state.password_repeat) {
      this.setState({opt_message: 'Passwords need to be the same'});
    } else {
      this.setState({opt_message: null});
      fetch(`http://${server_ip}:8080/register`, {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: this.state.login,
          password: this.state.password,
          email: this.state.email,
        }),
      })
        .then(message => {
          if (message.ok) {
            this.setState({opt_message: 'ok'});
            this.change_register();
          } else {
            this.setState({opt_message: 'failed to register'});
          }
        })
        .catch(e => console.log(e))
        .done();
    }
  };
  render() {
    if (this.state.token) {
      return (
        <ScrollView>
          <Card>
            <Card.Title title={`Hello ${this.state.loged_name}!`} titleStyle={{textAlign: 'center'}} />
            <Card.Actions>
              <Button style={{marginLeft: 'auto', marginRight: 'auto'}} mode='contained' icon="logout" onPress={this.logout}>Logout</Button>
            </Card.Actions>
          </Card>
        </ScrollView>
      );
    }
    if (this.state.need_register) {
      return (
        <ScrollView>
          <Card>
            <Card.Title title="Register" titleStyle={{textAlign: "center"}} />
            <Card.Content>
              <TextInput
                style={{marginBottom: 10}}
                placeholder="Email"
                name="email"
                value={this.state.email}
                onChangeText={text => this.setState({email: text})}
                />
              <TextInput
                style={{marginBottom: 10}}
                placeholder="Login"
                name="login"
                value={this.state.login}
                onChangeText={text => this.setState({login: text})}
                />
              <TextInput
                style={{marginBottom: 10}}
                secureTextEntry={true}
                placeholder="Password"
                name="password"
                value={this.state.password}
                onChangeText={text => this.setState({password: text})}
                />
              <TextInput
                secureTextEntry={true}
                placeholder="Repeat Password"
                name="password_repeat"
                value={this.state.password_repeat}
                onChangeText={text =>
                  this.setState({password_repeat: text})
                }
                />
                <Text style={{alignSelf: 'center'}}>
                  {this.state.opt_message}
                </Text>
            </Card.Content>
            <Card.Actions style={{marginLeft: "auto"}}>
              <Button onPress={this.register} mode="contained">
                register
              </Button>
              <Button
                onPress={this.change_register}>
                back to login
              </Button>
            </Card.Actions>
          </Card>
        </ScrollView>
      );
    }
    return (
      <ScrollView>
        <Card>
          <Card.Title title="Login" titleStyle={{textAlign: "center"}} />
          <Card.Content>
            <TextInput
              style={{marginBottom: 10}}
              placeholder="Login"
              name="login"
              value={this.state.login}
              onChangeText={text => this.setState({login: text})}
            />
            <TextInput
              style={{marginBottom: 10}}
              secureTextEntry={true}
              placeholder="Password"
              name="password"
              value={this.state.password}
              onChangeText={text => this.setState({password: text})}
            />
            <Text style={{alignSelf: 'center'}}>
              {this.state.opt_message}
            </Text>
          </Card.Content>
          <Card.Actions style={{marginLeft: 'auto'}}>
            <Button onPress={this.login} mode="contained">
              Login
            </Button>
            <Button onPress={this.change_register}>
              register
            </Button>
          </Card.Actions>
        </Card>
      </ScrollView>
    );
  }
}
