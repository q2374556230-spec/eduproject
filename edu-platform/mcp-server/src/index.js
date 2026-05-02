import 'dotenv/config';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';
import axios from 'axios';

const gateway = axios.create({
  baseURL: process.env.GATEWAY_URL || 'http://localhost:8080',
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
    ...(process.env.PLATFORM_TOKEN
      ? { Authorization: `Bearer ${process.env.PLATFORM_TOKEN}` }
      : {}),
  },
});

async function callApi(method, path, params = {}, body = null) {
  try {
    const res = await gateway.request({
      method,
      url: path,
      params: method === 'GET' ? params : undefined,
      data: method !== 'GET' ? body : undefined,
    });
    return res.data;
  } catch (err) {
    const msg = err.response?.data?.message || err.message;
    return { error: true, message: msg };
  }
}

const server = new McpServer({
  name: 'edu-platform',
  version: '1.0.0',
});

server.tool(
  'search_courses',
  'Search published courses with optional keyword, category, level, sorting, and pagination.',
  {
    keyword: z.string().optional().describe('Keyword matching course title or description'),
    categoryId: z.number().int().optional().describe('Course category id'),
    level: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']).optional().describe('Course level'),
    sortBy: z.enum(['newest', 'hottest', 'price_asc', 'price_desc']).optional().describe('Sort mode'),
    page: z.number().int().min(1).default(1).describe('Page number, starting at 1'),
    size: z.number().int().min(1).max(50).default(10).describe('Page size, max 50'),
  },
  async ({ keyword, categoryId, level, sortBy, page, size }) => {
    const sortMap = {
      newest: 'created_at',
      hottest: 'student_count',
      price_asc: 'price',
      price_desc: 'price',
    };
    const orderDirMap = {
      price_asc: 'asc',
      price_desc: 'desc',
    };
    const data = await callApi('GET', '/api/course/list', {
      keyword,
      categoryId,
      level: level?.toLowerCase(),
      orderBy: sortBy ? sortMap[sortBy] : undefined,
      orderDir: sortBy ? (orderDirMap[sortBy] || 'desc') : undefined,
      page,
      size,
    });
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'get_course_detail',
  'Get full course details by course id.',
  {
    courseId: z.number().int().positive().describe('Course id'),
  },
  async ({ courseId }) => {
    const data = await callApi('GET', `/api/course/${courseId}`);
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'get_course_categories',
  'Get all course categories.',
  {},
  async () => {
    const data = await callApi('GET', '/api/course/category/list');
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'get_ai_recommendations',
  'Get AI course recommendations for an interest or learning goal.',
  {
    interest: z.string().optional().describe('User interest or learning goal'),
    limit: z.number().int().min(1).max(20).default(5).describe('Maximum recommendations returned by this tool'),
  },
  async ({ interest, limit }) => {
    const data = await callApi('GET', '/api/course/recommend', { interest });
    if (data?.data && Array.isArray(data.data)) {
      data.data = data.data.slice(0, limit);
    }
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'get_order_stats',
  'Get platform order statistics, including totals, revenue, and status counts.',
  {},
  async () => {
    const data = await callApi('GET', '/api/order/admin/stats');
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'list_orders',
  'Admin tool: list all orders with optional status filter and pagination.',
  {
    status: z.number().int().min(0).max(3).optional().describe('0=pending, 1=paid, 2=cancelled, 3=refunded'),
    page: z.number().int().min(1).default(1).describe('Page number'),
    size: z.number().int().min(1).max(50).default(10).describe('Page size'),
  },
  async ({ status, page, size }) => {
    const data = await callApi('GET', '/api/order/admin/list', { status, page, size });
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'list_users',
  'Admin tool: list platform users with optional keyword and pagination.',
  {
    keyword: z.string().optional().describe('Keyword matching username, email, or real name'),
    page: z.number().int().min(1).default(1).describe('Page number'),
    size: z.number().int().min(1).max(50).default(10).describe('Page size'),
  },
  async ({ keyword, page, size }) => {
    const data = await callApi('GET', '/api/user/admin/list', { keyword, page, size });
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'get_user_detail',
  'Get user details by user id.',
  {
    userId: z.number().int().positive().describe('User id'),
  },
  async ({ userId }) => {
    const data = await callApi('GET', `/api/user/${userId}`);
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.tool(
  'get_user_notifications',
  'Get notifications for a specified user.',
  {
    userId: z.number().int().positive().describe('User id'),
    unreadOnly: z.boolean().default(false).describe('Only return unread notifications'),
    page: z.number().int().min(1).default(1).describe('Page number'),
    size: z.number().int().min(1).max(50).default(10).describe('Page size'),
  },
  async ({ userId, unreadOnly, page, size }) => {
    const data = await callApi('GET', '/api/notify/list', { userId, unreadOnly, page, size });
    return { content: [{ type: 'text', text: JSON.stringify(data, null, 2) }] };
  },
);

server.resource(
  'platform-api-doc',
  'edu-platform://api-overview',
  async (uri) => ({
    contents: [{
      uri: uri.href,
      mimeType: 'text/markdown',
      text: `# edu-platform API Overview

Gateway base URL: \`http://localhost:8080\`

Public APIs:
- \`POST /api/user/register\`
- \`POST /api/user/login\`
- \`GET /api/course/list\`
- \`GET /api/course/category/list\`

Authenticated APIs require \`Authorization: Bearer <JWT>\`.
MCP tools call the same gateway routes used by the frontend.
`,
    }],
  }),
);

const transport = new StdioServerTransport();
await server.connect(transport);
console.error('[edu-platform MCP] Server started (stdio mode)');
