// src/store/index.js
import Vue from 'vue';
import Vuex from 'vuex';
import {jwtDecode} from "jwt-decode";

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        token: sessionStorage.getItem('token') || '',
        user: {}
    },
    mutations: {
        setToken(state, token) {
            state.token = token;
            console.log(token);
            if (token) {
                state.user = jwtDecode(token);
                console.log(state.user);
            } else {
                state.user = {};
            }
        }
    },
    actions: {
        login({ commit }, token) {
            commit('setToken', token);
        },
        logout({ commit }) {
            commit('setToken', '');
        }
    },
    getters: {
        token: state => state.token,
        user: state => state.user
    }
});
