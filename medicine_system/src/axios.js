import axios from 'axios';

const createAxiosInstance = (baseURL, timeout, maxRetries = 3) => {
    const instance = axios.create({
        baseURL,
        timeout,
    });

    instance.interceptors.response.use(
        response => response,
        async error => {
            const originalRequest = error.config;

            if (error.code === 'ECONNABORTED' && originalRequest.retries < maxRetries) {
                originalRequest.retries += 1;
                return instance(originalRequest);
            }

            return Promise.reject(error);
        }
    );

    return instance;
};

export const apiRear = createAxiosInstance('http://localhost:8080', 10000);
export const apiFront = createAxiosInstance('http://localhost:7070', 10000);