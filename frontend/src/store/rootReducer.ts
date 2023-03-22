import { combineReducers } from '@reduxjs/toolkit';
import themeReducer from '@/store/slices/themeSlice';
import testSlice from '@/store/slices/testSlice';

const rootReducer = combineReducers({ isDark: themeReducer, test: testSlice });

export default rootReducer;
