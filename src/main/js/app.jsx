import React from 'react';
import {render} from 'react-dom';
import Popup from "./dialog/Popup";

class App extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (<h3>this is me react</h3>)
    }
}

// render(<App/>, document.getElementById('popup'));
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
    document.getElementById('popup')
);

