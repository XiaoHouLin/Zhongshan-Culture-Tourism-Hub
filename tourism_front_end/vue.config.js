const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 7070,
  },
  chainWebpack: config => {
    // 添加一个规则来处理 .ico 文件
    config.module.rule('icons')
        .test(/\.ico$/)
        .use('file-loader')
        .loader('file-loader')
        .options({
          name: '[name].[ext]',
          outputPath: 'static/icons/',
          publicPath: 'static/icons/'
        });
  }
})
