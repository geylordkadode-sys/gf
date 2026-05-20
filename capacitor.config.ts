import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.berling.marketplace',
  appName: 'Berling MarketHub',
  webDir: 'build',
  server: {
    androidScheme: 'https'
  },
  plugins: {
    Camera: {
      permissions: ['photos']
    },
    Filesystem: {
      permissions: ['public', 'documents']
    }
  }
};

export default config;
