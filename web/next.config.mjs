/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/users/:path*',
        destination: 'http://localhost:8081/api/users/:path*',
      },
      {
        source: '/api/contracts/:path*',
        destination: 'http://localhost:8082/api/contracts/:path*',
      },
    ];
  },
};

export default nextConfig;
