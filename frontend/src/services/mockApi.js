const MOCK_DATA = [
  { id: 1, name: 'Kamal Perera', nicNumber: '901234567V', dateOfBirth: '1990-05-15', email: 'kamal.p@example.com', mobileNumbers: [{ mobileNumber: '0771234567' }], addresses: [{ addressLine1: '12 Temple Road' }] },
  { id: 2, name: 'Sunil Silva', nicNumber: '852233445V', dateOfBirth: '1985-02-20', email: 'sunil.s@example.com', mobileNumbers: [{ mobileNumber: '0779876543' }], addresses: [] },
  { id: 3, name: 'Nimali Jayasuriya', nicNumber: '928877665V', dateOfBirth: '1992-11-30', email: 'nimali.j@example.com', mobileNumbers: [], addresses: [] },
];

const mockApi = {
  customers: [...MOCK_DATA],
  getAll: () => Promise.resolve({ data: { data: { content: [...mockApi.customers] } } }),
  getById: (id) => Promise.resolve({ data: { data: mockApi.customers.find(item => item.id === Number(id)) } }),
  create: (data) => {
    const newCustomer = { ...data, id: Date.now() };
    mockApi.customers.unshift(newCustomer);
    return Promise.resolve({ data: { data: newCustomer } });
  },
  update: (id, data) => {
    const index = mockApi.customers.findIndex(customer => customer.id === Number(id));
    if (index !== -1) {
      mockApi.customers[index] = { ...mockApi.customers[index], ...data };
    }
    return Promise.resolve({ data: { data: mockApi.customers[index] } });
  },
  delete: (id) => {
    mockApi.customers = mockApi.customers.filter(customer => customer.id !== Number(id));
    return Promise.resolve({ success: true });
  }
};

export { MOCK_DATA };
export default mockApi;