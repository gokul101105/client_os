const CLASSES_BY_STATUS = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
};

export default function RequestStatusBadge({ status }) {
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
        CLASSES_BY_STATUS[status] ?? 'bg-gray-100 text-gray-600'
      }`}
    >
      {status}
    </span>
  );
}
