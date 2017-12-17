import React from 'react';
import {render} from 'react-dom';
import Popup from "./dialog/Popup";
import FriendListComponent from "./friendList/FriendListComponent";

class App extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (<h3>this is me react</h3>)
    }
}

// render(<App/>, document.getElementById('popup'));
let popupElement = document.getElementById('popup');
let friendListElement = document.getElementById('friendListComponent');

if(popupElement)
    render(
        <Popup visibility='hidden'
               toggleButtonValue='register'
               width='300'
               height='600'
               popupHeaderText='register form'
               root='/api'
               formName="register_form"
        >
        </Popup>,
        popupElement
    );

if(friendListElement)
    render (
        <FriendListComponent root='/api' friendMenuHeight='164' friendMenuWidth='360'/>,
        friendListElement
    );

